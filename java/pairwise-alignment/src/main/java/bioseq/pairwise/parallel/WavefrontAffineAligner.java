package bioseq.pairwise.parallel;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

/**
 * Global affine-gap aligner using anti-diagonal (wavefront) parallel dynamic programming.
 *
 * <p>Affine alignment uses three DP layers per cell:
 * <ul>
 *   <li>{@code D}: residue-residue state</li>
 *   <li>{@code I}: gap in sequence 2</li>
 *   <li>{@code S}: gap in sequence 1</li>
 * </ul>
 *
 * <p>For any cell {@code (i,j)}, all dependencies are on earlier anti-diagonals:
 * {@code (i-1,j-1)}, {@code (i-1,j)}, {@code (i,j-1)}. Therefore cells within the same
 * anti-diagonal are independent and can be computed in parallel once the previous anti-diagonal
 * is complete for all layers.
 */
public final class WavefrontAffineAligner implements GlobalAligner<AffineGapCost>, AutoCloseable {
  private static final int INF = Integer.MAX_VALUE / 2;
  private static final int LAYER_D = 0;
  private static final int LAYER_I = 1;
  private static final int LAYER_S = 2;
  private static final int LAYER_UNSET = -1;
  private static final int PARALLEL_CELL_THRESHOLD = 512;

  private final int numThreads;
  private final ForkJoinPool pool;
  private final boolean ownsPool;

  /**
   * Creates a wavefront affine aligner with a fixed worker count.
   *
   * @param numThreads number of worker threads to use for anti-diagonal work
   * @throws IllegalArgumentException if {@code numThreads} is not positive
   */
  public WavefrontAffineAligner(int numThreads) {
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
      // Reuse common pool when requested parallelism matches machine capacity.
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
  public int computeCost(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int alpha = gap.getAlpha();
    int beta = gap.getBeta();
    int openAndExtend = alpha + beta;

    int[][] d = new int[n + 1][m + 1];
    int[][] iLayer = new int[n + 1][m + 1];
    int[][] sLayer = new int[n + 1][m + 1];
    fillWithInfinity(d);
    fillWithInfinity(iLayer);
    fillWithInfinity(sLayer);

    d[0][0] = 0;
    for (int i = 1; i <= n; i++) {
      iLayer[i][0] = initialGapCost(alpha, beta, i);
    }
    for (int j = 1; j <= m; j++) {
      sLayer[0][j] = initialGapCost(alpha, beta, j);
    }

    // Process anti-diagonals in order so all d-1 dependencies are fully materialized first.
    for (int diagonal = 2; diagonal <= n + m; diagonal++) {
      int iMin = Math.max(1, diagonal - m);
      int iMax = Math.min(n, diagonal - 1);
      if (iMin > iMax) {
        continue;
      }

      int cellCount = iMax - iMin + 1;
      // Parallel execution is only worthwhile on sufficiently large anti-diagonals.
      if (pool == null || cellCount <= PARALLEL_CELL_THRESHOLD) {
        for (int i = iMin; i <= iMax; i++) {
          int j = diagonal - i;
          int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

          int bestDiagonalPredecessor = min3(d[i - 1][j - 1], iLayer[i - 1][j - 1], sLayer[i - 1][j - 1]);
          d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);

          int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
          int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
          iLayer[i][j] = Math.min(openInsertion, extendInsertion);

          int openSkip = safeAdd(d[i][j - 1], openAndExtend);
          int extendSkip = safeAdd(sLayer[i][j - 1], beta);
          sLayer[i][j] = Math.min(openSkip, extendSkip);
        }
      } else {
        final int dIdx = diagonal;
        final int start = iMin;
        final int end = iMax;
        // A single submitted task drives parallel per-cell work on this anti-diagonal.
        pool.submit(() ->
            IntStream.rangeClosed(start, end).parallel().forEach(i -> {
              int j = dIdx - i;
              int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

              int bestDiagonalPredecessor = min3(d[i - 1][j - 1], iLayer[i - 1][j - 1], sLayer[i - 1][j - 1]);
              d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);

              int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
              int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
              iLayer[i][j] = Math.min(openInsertion, extendInsertion);

              int openSkip = safeAdd(d[i][j - 1], openAndExtend);
              int extendSkip = safeAdd(sLayer[i][j - 1], beta);
              sLayer[i][j] = Math.min(openSkip, extendSkip);
            })
        ).join();
      }
    }

    return min3(d[n][m], iLayer[n][m], sLayer[n][m]);
  }

  @Override
  public AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    validateInputs(s1, s2, matrix, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int m = b.length();

    int alpha = gap.getAlpha();
    int beta = gap.getBeta();
    int openAndExtend = alpha + beta;

    int[][] d = new int[n + 1][m + 1];
    int[][] iLayer = new int[n + 1][m + 1];
    int[][] sLayer = new int[n + 1][m + 1];
    int[][] backD = new int[n + 1][m + 1];
    int[][] backI = new int[n + 1][m + 1];
    int[][] backS = new int[n + 1][m + 1];

    fillWithInfinity(d);
    fillWithInfinity(iLayer);
    fillWithInfinity(sLayer);
    fillWithUnset(backD);
    fillWithUnset(backI);
    fillWithUnset(backS);

    d[0][0] = 0;
    for (int i = 1; i <= n; i++) {
      iLayer[i][0] = initialGapCost(alpha, beta, i);
      backI[i][0] = (i == 1) ? LAYER_D : LAYER_I;
    }
    for (int j = 1; j <= m; j++) {
      sLayer[0][j] = initialGapCost(alpha, beta, j);
      backS[0][j] = (j == 1) ? LAYER_D : LAYER_S;
    }

    // Fill each anti-diagonal after all predecessor diagonals are complete.
    for (int diagonal = 2; diagonal <= n + m; diagonal++) {
      int iMin = Math.max(1, diagonal - m);
      int iMax = Math.min(n, diagonal - 1);
      if (iMin > iMax) {
        continue;
      }

      int cellCount = iMax - iMin + 1;
      if (pool == null || cellCount <= PARALLEL_CELL_THRESHOLD) {
        for (int i = iMin; i <= iMax; i++) {
          int j = diagonal - i;
          int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

          int bestDiagonalPredecessor = d[i - 1][j - 1];
          int bestDiagonalLayer = LAYER_D;
          if (iLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
            bestDiagonalPredecessor = iLayer[i - 1][j - 1];
            bestDiagonalLayer = LAYER_I;
          }
          if (sLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
            bestDiagonalPredecessor = sLayer[i - 1][j - 1];
            bestDiagonalLayer = LAYER_S;
          }
          d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);
          backD[i][j] = bestDiagonalLayer;

          int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
          int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
          if (extendInsertion < openInsertion) {
            iLayer[i][j] = extendInsertion;
            backI[i][j] = LAYER_I;
          } else {
            iLayer[i][j] = openInsertion;
            backI[i][j] = LAYER_D;
          }

          int openSkip = safeAdd(d[i][j - 1], openAndExtend);
          int extendSkip = safeAdd(sLayer[i][j - 1], beta);
          if (extendSkip < openSkip) {
            sLayer[i][j] = extendSkip;
            backS[i][j] = LAYER_S;
          } else {
            sLayer[i][j] = openSkip;
            backS[i][j] = LAYER_D;
          }
        }
      } else {
        final int dIdx = diagonal;
        final int start = iMin;
        final int end = iMax;
        // A single submitted task drives parallel per-cell work on this anti-diagonal.
        pool.submit(() ->
            IntStream.rangeClosed(start, end).parallel().forEach(i -> {
              int j = dIdx - i;
              int substitution = matrix.cost(a.charAt(i - 1), b.charAt(j - 1));

              int bestDiagonalPredecessor = d[i - 1][j - 1];
              int bestDiagonalLayer = LAYER_D;
              if (iLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
                bestDiagonalPredecessor = iLayer[i - 1][j - 1];
                bestDiagonalLayer = LAYER_I;
              }
              if (sLayer[i - 1][j - 1] < bestDiagonalPredecessor) {
                bestDiagonalPredecessor = sLayer[i - 1][j - 1];
                bestDiagonalLayer = LAYER_S;
              }
              d[i][j] = safeAdd(bestDiagonalPredecessor, substitution);
              backD[i][j] = bestDiagonalLayer;

              int openInsertion = safeAdd(d[i - 1][j], openAndExtend);
              int extendInsertion = safeAdd(iLayer[i - 1][j], beta);
              if (extendInsertion < openInsertion) {
                iLayer[i][j] = extendInsertion;
                backI[i][j] = LAYER_I;
              } else {
                iLayer[i][j] = openInsertion;
                backI[i][j] = LAYER_D;
              }

              int openSkip = safeAdd(d[i][j - 1], openAndExtend);
              int extendSkip = safeAdd(sLayer[i][j - 1], beta);
              if (extendSkip < openSkip) {
                sLayer[i][j] = extendSkip;
                backS[i][j] = LAYER_S;
              } else {
                sLayer[i][j] = openSkip;
                backS[i][j] = LAYER_D;
              }
            })
        ).join();
      }
    }

    int bestCost = d[n][m];
    int layer = LAYER_D;
    if (iLayer[n][m] < bestCost) {
      bestCost = iLayer[n][m];
      layer = LAYER_I;
    }
    if (sLayer[n][m] < bestCost) {
      bestCost = sLayer[n][m];
      layer = LAYER_S;
    }

    // Traceback is sequential: follow one selected predecessor chain to the origin.
    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = m;
    while (i > 0 || j > 0) {
      if (layer == LAYER_D) {
        if (i <= 0 || j <= 0) {
          throw new IllegalStateException("Invalid D-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append(a.charAt(i - 1));
        aligned2.append(b.charAt(j - 1));
        int previousLayer = backD[i][j];
        i--;
        j--;
        layer = previousLayer;
      } else if (layer == LAYER_I) {
        if (i <= 0) {
          throw new IllegalStateException("Invalid I-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        int previousLayer = backI[i][j];
        i--;
        layer = previousLayer;
      } else if (layer == LAYER_S) {
        if (j <= 0) {
          throw new IllegalStateException("Invalid S-layer traceback state at (" + i + "," + j + ")");
        }
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        int previousLayer = backS[i][j];
        j--;
        layer = previousLayer;
      } else {
        throw new IllegalStateException("Traceback layer not set at (" + i + "," + j + ")");
      }
    }

    return new AlignmentResult(bestCost, aligned1.reverse().toString(), aligned2.reverse().toString());
  }

  /** Validates inputs before dynamic-programming computation. */
  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix matrix, AffineGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(matrix, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    matrix.alphabet().validate(s1.getResidues());
    matrix.alphabet().validate(s2.getResidues());
  }

  /** Fills all cells of a 2D int array with the infinity sentinel. */
  private static void fillWithInfinity(int[][] matrix) {
    for (int[] row : matrix) {
      Arrays.fill(row, INF);
    }
  }

  /** Fills all cells of a 2D int array with unset layer markers. */
  private static void fillWithUnset(int[][] matrix) {
    for (int[] row : matrix) {
      Arrays.fill(row, LAYER_UNSET);
    }
  }

  /** Computes alpha + beta * length and clamps unreachable/overflow values to INF. */
  private static int initialGapCost(int alpha, int beta, int length) {
    long value = (long) alpha + (long) beta * length;
    return value >= INF ? INF : (int) value;
  }

  /** Adds a delta to a DP value while preserving INF as an unreachable sentinel. */
  private static int safeAdd(int value, int delta) {
    if (value >= INF) {
      return INF;
    }
    long sum = (long) value + delta;
    if (sum >= INF) {
      return INF;
    }
    if (sum <= -INF) {
      return -INF;
    }
    return (int) sum;
  }

  /** Returns the minimum of three int values. */
  private static int min3(int a, int b, int c) {
    return Math.min(a, Math.min(b, c));
  }
}
