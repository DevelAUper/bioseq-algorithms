package bioseq.core.gap;

/**
 * Linear gap-penalty model defined as {@code cost(k) = penalty * k}.
 *
 * <p>Use this model when every additional gap symbol has the same marginal cost. It is simple
 * and efficient, and is the default model for the global linear aligner in this project.
 */
public final class LinearGapCost implements GapCost {
  private final int penalty;

  /**
   * Creates a linear gap model.
   *
   * @param penalty per-symbol gap penalty
   * @throws IllegalArgumentException if {@code penalty} is negative
   */
  public LinearGapCost(int penalty) {
    if (penalty < 0) {
      throw new IllegalArgumentException("Gap penalty must be non-negative, got: " + penalty);
    }
    this.penalty = penalty;
  }

  /**
   * Computes the linear penalty for a gap of length {@code length}.
   *
   * @param length contiguous gap length
   * @return {@code length * penalty}
   * @throws IllegalArgumentException if {@code length} is negative
   */
  @Override
  public int cost(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("Gap length must be non-negative, got: " + length);
    }
    return length * penalty;
  }

  /**
   * Returns the per-symbol linear gap penalty.
   *
   * @return non-negative linear gap penalty
   */
  public int getPenalty() {
    return penalty;
  }
}
