package bioseq.pairwise.global;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.util.Objects;

/**
 * Needleman-Wunsch style global aligner with a linear gap model.
 *
 * <p>This implementation minimizes total alignment cost using dynamic programming and can either
 * return only the optimal cost or reconstruct one optimal alignment via traceback.
 *
 * <p>Use this class when a constant per-gap-symbol penalty is appropriate.
 */
public final class GlobalLinearAligner implements GlobalAligner<LinearGapCost> {
  /**
   * Computes the minimum global alignment cost without reconstructing aligned strings.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param m substitution/distance matrix
   * @param gap linear gap-cost model
   * @return minimum total alignment cost
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  @Override
  public int computeCost(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dp = new int[n + 1][mLen + 1];

    // Base column: align prefix a[0..i) against empty b by inserting i gaps in sequence 2.
    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
    }
    // Base row: align empty a against prefix b[0..j) by inserting j gaps in sequence 1.
    for (int j = 1; j <= mLen; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
    }

    // If either sequence is empty, initialization already contains the full optimal cost.
    if (n == 0 || mLen == 0) {
      return dp[n][mLen];
    }

    // Fill DP table using three-way recurrence for each prefix pair (i, j):
    // 1) diagonal: align a[i-1] with b[j-1]
    // 2) up: align a[i-1] with a gap in b
    // 3) left: align b[j-1] with a gap in a
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dp[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
        int up = dp[i - 1][j] + gap.cost(1);
        int left = dp[i][j - 1] + gap.cost(1);
        // Min-cost global alignment objective.
        dp[i][j] = Math.min(diag, Math.min(up, left));
      }
    }
    return dp[n][mLen];
  }

  /**
   * Computes minimum cost and reconstructs one optimal global alignment.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param m substitution/distance matrix
   * @param gap linear gap-cost model
   * @return alignment result with optimal cost and one optimal aligned sequence pair
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  @Override
  public AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dp = new int[n + 1][mLen + 1];
    Move[][] back = new Move[n + 1][mLen + 1];

    // Initialize border states and remember the only possible predecessor on borders.
    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
      back[i][0] = Move.UP;
    }
    for (int j = 1; j <= mLen; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
      back[0][j] = Move.LEFT;
    }

    // Fill cost and traceback tables together using the same three-way recurrence.
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dp[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
        int up = dp[i - 1][j] + gap.cost(1);
        int left = dp[i][j - 1] + gap.cost(1);

        // Deterministic tie-breaking: DIAG preferred over UP, UP preferred over LEFT.
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
    }

    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = mLen;

    // Walk backwards from dp[n][mLen] to dp[0][0], appending aligned symbols in reverse.
    while (i > 0 || j > 0) {
      Move move = back[i][j];
      if (move == Move.DIAG) {
        // Match/mismatch: consume one symbol from each sequence.
        aligned1.append(a.charAt(i - 1));
        aligned2.append(b.charAt(j - 1));
        i--;
        j--;
      } else if (move == Move.UP) {
        // Gap in sequence 2: consume one symbol from sequence 1.
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        i--;
      } else if (move == Move.LEFT) {
        // Gap in sequence 1: consume one symbol from sequence 2.
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        j--;
      } else if (i > 0 && j == 0) {
        // Defensive border fallback when only sequence 1 has remaining symbols.
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        i--;
      } else if (j > 0) {
        // Defensive border fallback when only sequence 2 has remaining symbols.
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        j--;
      } else {
        throw new IllegalStateException("Traceback failed at cell (" + i + "," + j + ")");
      }
    }

    return new AlignmentResult(
        dp[n][mLen],
        aligned1.reverse().toString(),
        aligned2.reverse().toString());
  }

  /** Validates nullness and alphabet compatibility before DP computation. */
  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(m, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    m.alphabet().validate(s1.getResidues());
    m.alphabet().validate(s2.getResidues());
  }
}
