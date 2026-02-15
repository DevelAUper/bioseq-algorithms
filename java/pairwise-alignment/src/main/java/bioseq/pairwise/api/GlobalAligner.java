package bioseq.pairwise.api;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.model.AlignmentResult;

/**
 * Global aligner contract for two sequences using min-cost dynamic programming.
 *
 * <p>Type parameter {@code G} specifies the gap cost model (e.g. linear, affine).
 *
 * @param <G> the gap cost type this aligner accepts
 */
public interface GlobalAligner<G extends GapCost> {
  /**
   * Computes only the optimal alignment cost (no traceback).
   *
   * @param s1  first sequence
   * @param s2  second sequence
   * @param m   substitution/distance score matrix
   * @param gap gap cost function
   * @return the minimum alignment cost
   */
  int computeCost(Sequence s1, Sequence s2, ScoreMatrix m, G gap);

  /**
   * Computes optimal cost and reconstructs one optimal alignment via traceback.
   *
   * @param s1  first sequence
   * @param s2  second sequence
   * @param m   substitution/distance score matrix
   * @param gap gap cost function
   * @return an {@link AlignmentResult} containing cost and aligned strings
   */
  AlignmentResult align(Sequence s1, Sequence s2, ScoreMatrix m, G gap);
}
