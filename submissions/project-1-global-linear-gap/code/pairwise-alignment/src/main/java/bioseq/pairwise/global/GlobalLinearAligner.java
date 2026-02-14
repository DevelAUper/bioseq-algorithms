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

    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dp[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
        int up = dp[i - 1][j] + gap.cost(1);
        int left = dp[i][j - 1] + gap.cost(1);
        dp[i][j] = Math.min(diag, Math.min(up, left));
      }
    }
    return dp[n][mLen];
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

    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= mLen; j++) {
        int diag = dp[i - 1][j - 1] + m.cost(a.charAt(i - 1), b.charAt(j - 1));
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
    }

    StringBuilder aligned1 = new StringBuilder();
    StringBuilder aligned2 = new StringBuilder();
    int i = n;
    int j = mLen;
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
