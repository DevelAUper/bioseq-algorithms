package bioseq.pairwise.global;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;

/**
 * Counts the number of optimal global alignments (min-cost).
 */
public final class OptimalAlignmentCounter {
  public long count(Sequence a, Sequence b, ScoreMatrix matrix, GapCost gapCost) {
    // TODO: Implement score and count recurrences with tie handling.
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
