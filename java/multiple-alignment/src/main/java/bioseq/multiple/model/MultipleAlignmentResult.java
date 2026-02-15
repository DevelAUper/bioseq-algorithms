package bioseq.multiple.model;

import java.util.List;
import java.util.Objects;

/**
 * Immutable result container for a multiple-sequence alignment.
 *
 * <p>All aligned sequence strings must have equal length and correspond positionally to
 * {@code sequenceIds}.
 */
public final class MultipleAlignmentResult {
  private final int cost;
  private final List<String> alignedSequences;
  private final List<String> sequenceIds;

  /**
   * Creates a multiple alignment result.
   *
   * @param cost total alignment cost
   * @param alignedSequences aligned sequence strings (including gaps)
   * @param sequenceIds sequence identifiers matching {@code alignedSequences} order
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if list sizes mismatch or aligned lengths differ
   */
  public MultipleAlignmentResult(int cost, List<String> alignedSequences, List<String> sequenceIds) {
    Objects.requireNonNull(alignedSequences, "alignedSequences must not be null");
    Objects.requireNonNull(sequenceIds, "sequenceIds must not be null");
    if (alignedSequences.isEmpty()) {
      throw new IllegalArgumentException("alignedSequences must contain at least one sequence");
    }
    if (alignedSequences.size() != sequenceIds.size()) {
      throw new IllegalArgumentException(
          "alignedSequences and sequenceIds must have the same size, got "
              + alignedSequences.size() + " and " + sequenceIds.size());
    }

    int expectedLength = -1;
    for (int i = 0; i < alignedSequences.size(); i++) {
      String aligned = Objects.requireNonNull(
          alignedSequences.get(i), "aligned sequence at index " + i + " must not be null");
      String id = Objects.requireNonNull(
          sequenceIds.get(i), "sequence id at index " + i + " must not be null");
      if (id.isBlank()) {
        throw new IllegalArgumentException("sequence id at index " + i + " must not be blank");
      }
      if (expectedLength == -1) {
        expectedLength = aligned.length();
      } else if (aligned.length() != expectedLength) {
        throw new IllegalArgumentException(
            "All aligned sequences must have equal length; expected "
                + expectedLength + " but found " + aligned.length() + " at index " + i);
      }
    }

    this.cost = cost;
    this.alignedSequences = List.copyOf(alignedSequences);
    this.sequenceIds = List.copyOf(sequenceIds);
  }

  /**
   * Returns the total alignment cost.
   *
   * @return alignment cost
   */
  public int getCost() {
    return cost;
  }

  /**
   * Returns the aligned sequence strings in input order.
   *
   * @return immutable aligned sequence list
   */
  public List<String> getAlignedSequences() {
    return alignedSequences;
  }

  /**
   * Returns the sequence identifiers in the same order as aligned strings.
   *
   * @return immutable sequence-id list
   */
  public List<String> getSequenceIds() {
    return sequenceIds;
  }
}
