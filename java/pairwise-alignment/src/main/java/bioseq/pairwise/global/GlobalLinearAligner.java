package bioseq.pairwise.global;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.model.AlignmentResult;

/**
 * Global alignment with linear gap penalties (min-cost).
 */
public final class GlobalLinearAligner implements GlobalAligner {
  @Override
  public AlignmentResult align(Sequence a, Sequence b, ScoreMatrix matrix, GapCost gapCost) {
    int[][] dp = computeDp(a, b, matrix, gapCost);
    return backtrack(a, b, dp, matrix, gapCost);
  }

  private int[][] computeDp(Sequence a, Sequence b, ScoreMatrix matrix, GapCost gapCost) {
    // TODO: Implement DP recurrence for min-cost global alignment.
    throw new UnsupportedOperationException("Not implemented yet");
  }

  private AlignmentResult backtrack(Sequence a, Sequence b, int[][] dp, ScoreMatrix matrix, GapCost gapCost) {
    // TODO: Implement backtracking to recover an optimal alignment.
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
