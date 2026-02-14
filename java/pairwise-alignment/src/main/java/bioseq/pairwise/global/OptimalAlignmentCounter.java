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

    // TODO(Project 1 DP):
    // 1) Fill dpCost[i][j] using the min-cost recurrence (DIAG/UP/LEFT).
    // 2) Set dpCount[i][j] to sum of predecessor counts that achieve that minimum.
    //    For example, if DIAG and LEFT tie for min, dpCount[i][j] = countDiag + countLeft.
    throw new UnsupportedOperationException("TODO: implement optimal-alignment count DP recurrence");
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
