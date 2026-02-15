package bioseq.multiple.scoring;

import bioseq.core.gap.GapCost;
import bioseq.core.scoring.ScoreMatrix;
import java.util.List;
import java.util.Objects;

/**
 * Sum-of-pairs (SP) scoring for multiple sequence alignments.
 *
 * <p>For each alignment column, SP cost sums pairwise costs over all unordered sequence pairs.
 * Total alignment cost is the sum of SP column costs across all columns.
 *
 * <p>Gap handling in this implementation:
 * <ul>
 *   <li>gap vs residue: {@code gap.cost(1)}</li>
 *   <li>gap vs gap: {@code 0}</li>
 * </ul>
 */
public final class SumOfPairsCost {
  /**
   * Computes SP cost for one alignment column.
   *
   * @param column residues/gaps for one column across all sequences
   * @param matrix substitution/distance matrix
   * @param gap gap-cost model
   * @return sum-of-pairs column cost
   */
  public int columnCost(char[] column, ScoreMatrix matrix, GapCost gap) {
    Objects.requireNonNull(column, "column must not be null");
    Objects.requireNonNull(matrix, "matrix must not be null");
    Objects.requireNonNull(gap, "gap must not be null");

    int total = 0;
    for (int i = 0; i < column.length; i++) {
      for (int j = i + 1; j < column.length; j++) {
        char a = column[i];
        char b = column[j];
        if (a == '-' && b == '-') {
          continue;
        }
        if (a == '-' || b == '-') {
          total += gap.cost(1);
        } else {
          total += matrix.cost(a, b);
        }
      }
    }
    return total;
  }

  /**
   * Computes total SP cost for an entire multiple alignment.
   *
   * @param alignedSequences aligned sequence strings (all equal length)
   * @param matrix substitution/distance matrix
   * @param gap gap-cost model
   * @return total alignment SP cost
   */
  public int totalCost(List<String> alignedSequences, ScoreMatrix matrix, GapCost gap) {
    Objects.requireNonNull(alignedSequences, "alignedSequences must not be null");
    Objects.requireNonNull(matrix, "matrix must not be null");
    Objects.requireNonNull(gap, "gap must not be null");
    if (alignedSequences.isEmpty()) {
      throw new IllegalArgumentException("alignedSequences must contain at least one sequence");
    }

    int length = -1;
    for (int i = 0; i < alignedSequences.size(); i++) {
      String aligned = Objects.requireNonNull(
          alignedSequences.get(i), "aligned sequence at index " + i + " must not be null");
      if (length == -1) {
        length = aligned.length();
      } else if (aligned.length() != length) {
        throw new IllegalArgumentException(
            "All aligned sequences must have equal length; expected "
                + length + " but found " + aligned.length() + " at index " + i);
      }
    }

    int total = 0;
    int n = alignedSequences.size();
    char[] column = new char[n];
    for (int pos = 0; pos < length; pos++) {
      for (int row = 0; row < n; row++) {
        column[row] = alignedSequences.get(row).charAt(pos);
      }
      total += columnCost(column, matrix, gap);
    }
    return total;
  }
}
