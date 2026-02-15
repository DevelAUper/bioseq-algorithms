package bioseq.pairwise.global;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Counts how many distinct optimal global alignments exist under linear gap penalties.
 *
 * <p>The algorithm combines a cost DP with a counting DP:
 * {@code dpCost[i][j]} stores optimal cost for prefixes, and {@code dpCount[i][j]} stores
 * how many optimal paths achieve that cost.
 */
public final class OptimalAlignmentCounter {
  /**
   * Counts optimal alignments for two sequences.
   *
   * @param s1 first sequence
   * @param s2 second sequence
   * @param m substitution/distance matrix
   * @param gap linear gap-cost model
   * @return number of optimal alignments as {@link BigInteger}
   * @throws NullPointerException if any input is {@code null}
   * @throws IllegalArgumentException if a sequence contains symbols outside the matrix alphabet
   */
  public BigInteger countOptimalAlignments(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dpCost = new int[n + 1][mLen + 1];
    // Counts can grow combinatorially with sequence length, so fixed-width integers are unsafe.
    BigInteger[][] dpCount = new BigInteger[n + 1][mLen + 1];

    // Base case: one way to align two empty prefixes.
    dpCount[0][0] = BigInteger.ONE;
    // First column: only UP moves are possible, so each prefix has exactly one optimal path.
    for (int i = 1; i <= n; i++) {
      dpCost[i][0] = dpCost[i - 1][0] + gap.cost(1);
      dpCount[i][0] = BigInteger.ONE;
    }
    // First row: only LEFT moves are possible, so each prefix has exactly one optimal path.
    for (int j = 1; j <= mLen; j++) {
      dpCost[0][j] = dpCost[0][j - 1] + gap.cost(1);
      dpCount[0][j] = BigInteger.ONE;
    }

    // If one sequence is empty, only the border path exists.
    if (n == 0 || mLen == 0) {
      return BigInteger.ONE;
    }

    // Recurrence:
    // 1) compute best achievable cost from diagonal/up/left predecessors
    // 2) sum counts from exactly those predecessors that attain the best cost
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dpCost[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
        int up = dpCost[i - 1][j] + gap.cost(1);
        int left = dpCost[i][j - 1] + gap.cost(1);
        int best = Math.min(diag, Math.min(up, left));

        dpCost[i][j] = best;
        BigInteger count = BigInteger.ZERO;
        // Any predecessor that ties for best contributes all of its optimal paths.
        if (diag == best) {
          count = count.add(dpCount[i - 1][j - 1]);
        }
        if (up == best) {
          count = count.add(dpCount[i - 1][j]);
        }
        if (left == best) {
          count = count.add(dpCount[i][j - 1]);
        }
        dpCount[i][j] = count;
      }
    }

    return dpCount[n][mLen];
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
