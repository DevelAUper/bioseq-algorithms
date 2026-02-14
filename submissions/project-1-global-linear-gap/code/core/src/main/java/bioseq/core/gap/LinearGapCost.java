package bioseq.core.gap;

/**
 * Linear gap penalty: cost = length * penalty.
 */
public final class LinearGapCost implements GapCost {
  private final int penalty;

  public LinearGapCost(int penalty) {
    if (penalty < 0) {
      throw new IllegalArgumentException("Gap penalty must be non-negative, got: " + penalty);
    }
    this.penalty = penalty;
  }

  @Override
  public int cost(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("Gap length must be non-negative, got: " + length);
    }
    return length * penalty;
  }

  public int getPenalty() {
    return penalty;
  }
}
