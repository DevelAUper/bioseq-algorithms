package bioseq.pairwise.global;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Counts the number of optimal global alignments (min-cost).
 */
public final class OptimalAlignmentCounter {
  public BigInteger countOptimalAlignments(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dpCost = new int[n + 1][mLen + 1];
    BigInteger[][] dpCount = new BigInteger[n + 1][mLen + 1];

    dpCount[0][0] = BigInteger.ONE;
    for (int i = 1; i <= n; i++) {
      dpCost[i][0] = dpCost[i - 1][0] + gap.cost(1);
      dpCount[i][0] = BigInteger.ONE;
    }
    for (int j = 1; j <= mLen; j++) {
      dpCost[0][j] = dpCost[0][j - 1] + gap.cost(1);
      dpCount[0][j] = BigInteger.ONE;
    }

    if (n == 0 || mLen == 0) {
      return BigInteger.ONE;
    }

    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dpCost[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
        int up = dpCost[i - 1][j] + gap.cost(1);
        int left = dpCost[i][j - 1] + gap.cost(1);
        int best = Math.min(diag, Math.min(up, left));

        dpCost[i][j] = best;
        BigInteger count = BigInteger.ZERO;
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

  private static void validateInputs(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    Objects.requireNonNull(s1, "s1 must not be null");
    Objects.requireNonNull(s2, "s2 must not be null");
    Objects.requireNonNull(m, "score matrix must not be null");
    Objects.requireNonNull(gap, "gap cost must not be null");
    m.alphabet().validate(s1.getResidues());
    m.alphabet().validate(s2.getResidues());
  }
}
