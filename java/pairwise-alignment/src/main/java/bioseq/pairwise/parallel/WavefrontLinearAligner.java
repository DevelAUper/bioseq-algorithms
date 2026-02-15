package bioseq.pairwise.parallel;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.global.Move;
import bioseq.pairwise.model.AlignmentResult;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * Global linear-gap aligner using wavefront (anti-diagonal) parallel dynamic programming.
 *
 * <p>For the standard recurrence, cell {@code (i,j)} depends only on
 * {@code (i-1,j-1)}, {@code (i-1,j)}, and {@code (i,j-1)}. All of those predecessors are on earlier
 * anti-diagonals ({@code i+j-1} or {@code i+j-2}), so cells on the same anti-diagonal
 * ({@code i+j=d}) are independent and can be computed in parallel.
 *
 * <p>This implementation processes anti-diagonals sequentially to preserve dependencies, and
 * parallelizes within each anti-diagonal when it is large enough to amortize scheduling overhead.
 */
public final class WavefrontLinearAligner implements GlobalAligner<LinearGapCost>, AutoCloseable {
  private static final int PARALLEL_CELL_THRESHOLD = 512;

  private final int numThreads;
  private final ForkJoinPool pool;
  private final boolean ownsPool;

  /**
   * Creates a wavefront aligner with a fixed worker count.
   *
   * @param numThreads number of worker threads to use for anti-diagonal work
   * @throws IllegalArgumentException if {@code numThreads} is not positive
   */
  public WavefrontLinearAligner(int numThreads) {
    if (numThreads <= 0) {
      throw new IllegalArgumentException("numThreads must be positive, got: " + numThreads);
    }
    this.numThreads = numThreads;
    if (numThreads == 1) {
      this.pool = null;
      this.ownsPool = false;
      return;
    }

    int available = Runtime.getRuntime().availableProcessors();
    if (numThreads == available) {
      // Reuse the common pool when requested parallelism matches machine capacity.
      this.pool = ForkJoinPool.commonPool();
      this.ownsPool = false;
    } else {
      // Otherwise keep one dedicated pool and reuse it across method calls.
      this.pool = new ForkJoinPool(numThreads);
      this.ownsPool = true;
    }
  }

  /**
   * Shuts down the dedicated pool when this aligner owns one.
   *
   * <p>No-op when running sequentially or when using the common pool.
   */
  public void shutdown() {
    if (ownsPool) {
      pool.shutdown();
    }
  }

  /**
   * Alias for {@link #shutdown()} to support try-with-resources usage.
   */
  @Override
  public void close() {
    shutdown();
  }

  @Override
  public int computeCost(Sequence s1, Sequence s2, ScoreMatrix matrix, LinearGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int[][] dp = new int[n + 1][m + 1];
    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
    }
    for (int j = 1; j <= m; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
    }

    if (n == 0 || m == 0) {
      return dp[n][m];
    }

    // Process anti-diagonals in order: each diagonal depends only on earlier diagonals.
    for (int d = 2; d <= n + m; d++) {
      int iMin = Math.max(1, d - m);
      int iMax = Math.min(n, d - 1);
      if (iMin > iMax) {
        continue;
      }

      int cellCount = iMax - iMin + 1;
      // Small diagonals are cheaper to evaluate sequentially than to schedule in parallel.
      if (numThreads <= 1 || cellCount <= PARALLEL_CELL_THRESHOLD) {
        for (int i = iMin; i <= iMax; i++) {
          int j = d - i;
          int diag = dp[i - 1][j - 1] + matrix.cost(a.charAt(i - 1), b.charAt(j - 1));
          int up = dp[i - 1][j] + gap.cost(1);
          int left = dp[i][j - 1] + gap.cost(1);
          dp[i][j] = Math.min(diag, Math.min(up, left));
        }
      } else {
        final int diagonal = d;
        final int start = iMin;
        final int end = iMax;
        // A single submitted task drives parallel per-cell work on this anti-diagonal.
        pool.submit(() ->
            IntStream.rangeClosed(start, end).parallel().forEach(i -> {
              int j = diagonal - i;
              int diagCost = dp[i - 1][j - 1] + matrix.cost(a.charAt(i - 1), b.charAt(j - 1));
              int up = dp[i - 1][j] + gap.cost(1);
              int left = dp[i][j - 1] + gap.cost(1);
              dp[i][j] = Math.min(diagCost, Math.min(up, left));
            })
        ).join();
      }
    }

    return dp[n][m];
  }

  @Override
  public AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix matrix, LinearGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int[][] dp = new int[n + 1][m + 1];
    Move[][] back = new Move[n + 1][m + 1];
    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
      back[i][0] = Move.UP;
    }
    for (int j = 1; j <= m; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
      back[0][j] = Move.LEFT;
    }

    // As in computeCost, anti-diagonals are processed in dependency order.
    for (int d = 2; d <= n + m; d++) {
      int iMin = Math.max(1, d - m);
      int iMax = Math.min(n, d - 1);
      if (iMin > iMax) {
        continue;
      }

      int cellCount = iMax - iMin + 1;
      if (numThreads <= 1 || cellCount <= PARALLEL_CELL_THRESHOLD) {
        for (int i = iMin; i <= iMax; i++) {
          int j = d - i;
          int diag = dp[i - 1][j - 1] + matrix.cost(a.charAt(i - 1), b.charAt(j - 1));
          int up = dp[i - 1][j] + gap.cost(1);
          int left = dp[i][j - 1] + gap.cost(1);

          int best = diag;
          Move bestMove = Move.DIAG;
          if (up < best) {
            best = up;
            bestMove = Move.UP;
          }
          if (left < best) {
            best = left;
            bestMove = Move.LEFT;
          }
          dp[i][j] = best;
          back[i][j] = bestMove;
        }
      } else {
        final int diagonal = d;
        final int start = iMin;
        final int end = iMax;
        // A single submitted task drives parallel per-cell work on this anti-diagonal.
        pool.submit(() ->
            IntStream.rangeClosed(start, end).parallel().forEach(i -> {
              int j = diagonal - i;
              int diagCost = dp[i - 1][j - 1] + matrix.cost(a.charAt(i - 1), b.charAt(j - 1));
              int up = dp[i - 1][j] + gap.cost(1);
              int left = dp[i][j - 1] + gap.cost(1);

              int best = diagCost;
              Move bestMove = Move.DIAG;
              if (up < best) {
                best = up;
                bestMove = Move.UP;
              }
              if (left < best) {
                best = left;
                bestMove = Move.LEFT;
              }
              dp[i][j] = best;
              back[i][j] = bestMove;
            })
        ).join();
      }
    }

    // Traceback remains sequential because it follows one path from (n,m) to (0,0).
    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = m;
    while (i > 0 || j > 0) {
      Move move = back[i][j];
      if (move == Move.DIAG) {
        aligned1.append(a.charAt(i - 1));
        aligned2.append(b.charAt(j - 1));
        i--;
        j--;
      } else if (move == Move.UP) {
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        i--;
      } else if (move == Move.LEFT) {
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        j--;
      } else if (i > 0 && j == 0) {
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        i--;
      } else if (j > 0) {
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        j--;
      } else {
        throw new IllegalStateException("Traceback failed at cell (" + i + "," + j + ")");
      }
    }

    return new AlignmentResult(dp[n][m], aligned1.reverse().toString(), aligned2.reverse().toString());
  }

  /** Validates nullness and alphabet compatibility before DP computation. */
  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix matrix, LinearGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(matrix, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    matrix.alphabet().validate(s1.getResidues());
    matrix.alphabet().validate(s2.getResidues());
  }
}
