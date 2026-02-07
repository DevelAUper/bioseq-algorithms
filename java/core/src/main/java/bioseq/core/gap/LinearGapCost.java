package bioseq.core.gap;

/**
 * Linear gap penalty: cost = length * penalty.
 */
public final class LinearGapCost implements GapCost {
  private final int penalty;

  public LinearGapCost(int penalty) {
    this.penalty = penalty;
  }

  @Override
  public int cost(int length) {
    return length * penalty;
  }

  public int getPenalty() {
    return penalty;
  }
}
