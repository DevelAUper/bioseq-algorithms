package bioseq.pairwise.api;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.model.AlignmentResult;

/**
 * Global aligner for two sequences using min-cost dynamic programming.
 */
public interface GlobalAligner {
  AlignmentResult align(Sequence a, Sequence b, ScoreMatrix matrix, GapCost gapCost);
}
