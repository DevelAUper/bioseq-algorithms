package bioseq.pairwise.global;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.util.Objects;

/**
 * Global alignment with linear gap penalties (min-cost).
 */
public final class GlobalLinearAligner implements GlobalAligner {
  @Override
  public int computeCost(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dp = new int[n + 1][mLen + 1];
    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
    }
    for (int j = 1; j <= mLen; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
    }

    if (n == 0 || mLen == 0) {
      return dp[n][mLen];
    }

    // TODO(Project 1 DP): Fill the interior recurrence.
    // dp[i][j] = min(
    //   dp[i - 1][j - 1] + matrix.cost(a_i, b_j),
    //   dp[i - 1][j] + gap.cost(1),
    //   dp[i][j - 1] + gap.cost(1)
    // )
    throw new UnsupportedOperationException("TODO: implement core DP recurrence for computeCost");
  }

  @Override
  public AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap) {
    validateInputs(s1, s2, m, gap);

    String a = s1.getResidues();
    String b = s2.getResidues();
    int n = a.length();
    int mLen = b.length();

    int[][] dp = new int[n + 1][mLen + 1];
    Move[][] back = new Move[n + 1][mLen + 1];

    for (int i = 1; i <= n; i++) {
      dp[i][0] = dp[i - 1][0] + gap.cost(1);
      back[i][0] = Move.UP;
    }
    for (int j = 1; j <= mLen; j++) {
      dp[0][j] = dp[0][j - 1] + gap.cost(1);
      back[0][j] = Move.LEFT;
    }

    if (n > 0 && mLen > 0) {
      // TODO(Project 1 DP): Fill interior costs and backpointers.
      // 1) Compute candidate costs from DIAG/UP/LEFT.
      // 2) Set dp[i][j] to the minimum candidate.
      // 3) For ties, choose deterministic priority DIAG > UP > LEFT for `back[i][j]`.
      throw new UnsupportedOperationException("TODO: implement DP recurrence and backpointer tie handling");
    }

    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = mLen;
    while (i > 0 || j > 0) {
      // TODO(Project 1 DP): Extend traceback using `back[i][j]` to reconstruct one optimal alignment.
      // DIAG consumes one char from both sequences, UP consumes one from s1 and inserts '-',
      // LEFT consumes one from s2 and inserts '-'. Append chars then reverse at the end.
      // Boundary fallback below keeps empty-vs-nonempty cases working until full traceback is added.
      if (i > 0 && j == 0) {
        aligned1.append(a.charAt(i - 1));
        aligned2.append('-');
        i--;
      } else if (j > 0 && i == 0) {
        aligned1.append('-');
        aligned2.append(b.charAt(j - 1));
        j--;
      } else {
        throw new UnsupportedOperationException("TODO: implement traceback reconstruction for interior cells");
      }
    }

    return new AlignmentResult(
        dp[n][mLen],
        aligned1.reverse().toString(),
        aligned2.reverse().toString());
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
