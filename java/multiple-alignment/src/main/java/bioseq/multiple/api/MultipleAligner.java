package bioseq.multiple.api;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.multiple.model.MultipleAlignmentResult;
import java.util.List;

/**
 * Contract for multiple-sequence alignment algorithms.
 *
 * <p>Unlike pairwise alignment, multiple alignment jointly aligns three or more sequences to
 * produce a single alignment matrix where all sequences share the same aligned length.
 *
 * <p>Implementations may be exact or heuristic, but they should consistently interpret costs using
 * the provided substitution matrix and gap model.
 */
public interface MultipleAligner {
  /**
   * Computes a multiple alignment and returns aligned strings with total cost.
   *
   * @param sequences input sequences to align
   * @param matrix substitution/distance score matrix
   * @param gap gap-cost model
   * @return alignment result containing cost and aligned sequences
   */
  MultipleAlignmentResult align(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap);

  /**
   * Computes only the alignment cost for the provided sequence set.
   *
   * @param sequences input sequences to align
   * @param matrix substitution/distance score matrix
   * @param gap gap-cost model
   * @return total alignment cost
   */
  int computeCost(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap);
}
