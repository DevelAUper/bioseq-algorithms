package bioseq.pairwise.api;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.model.AlignmentResult;

/**
 * Global aligner for two sequences using min-cost dynamic programming.
 */
public interface GlobalAligner {
  /**
   * Computes only the optimal alignment cost (no traceback).
   */
  int computeCost(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap);

  /**
   * Computes an optimal cost and reconstructs one optimal alignment via traceback.
   */
  AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix m, LinearGapCost gap);
}
