package bioseq.multiple.heuristic;

import bioseq.core.gap.GapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.multiple.api.MultipleAligner;
import bioseq.multiple.model.MultipleAlignmentResult;
import java.util.List;

/**
 * Placeholder scaffold for a center-star multiple alignment heuristic.
 *
 * <p>Center-star strategy overview:
 * <ol>
 *   <li>Compute all pairwise alignments/distances.</li>
 *   <li>Select the center sequence minimizing total distance to all others.</li>
 *   <li>Progressively align each remaining sequence to the center-induced profile.</li>
 * </ol>
 *
 * <p>This class is intentionally scaffolded for future implementation work.
 */
public final class CenterStarAligner implements MultipleAligner {
  @Override
  public MultipleAlignmentResult align(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap) {
    // TODO Step 1: Validate that at least 3 sequences are provided.
    // TODO Step 2: Compute all pairwise alignment costs between sequences.
    // TODO Step 3: Choose center sequence with minimum total pairwise distance.
    // TODO Step 4: Build progressive multiple alignment using center as anchor.
    // TODO Step 5: Compute final sum-of-pairs cost and return result container.
    return null;
  }

  @Override
  public int computeCost(List<Sequence> sequences, ScoreMatrix matrix, GapCost gap) {
    // TODO Step 1: Reuse the center-star construction from align(...).
    // TODO Step 2: Score resulting alignment with sum-of-pairs objective.
    // TODO Step 3: Return only the cost.
    return 0;
  }
}
