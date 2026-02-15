package bioseq.core.gap;

/**
 * Affine gap penalty: cost(k) = alpha + beta * k, for k >= 1.
 *
 * <p>{@code alpha} is the gap opening penalty and {@code beta} is the
 * gap extension penalty. For a gap of length zero the cost is zero.
 *
 * <p>This model penalizes opening a new gap more heavily than extending
 * an existing one, which is biologically more realistic than linear gap cost.
 * It implements the shared {@link GapCost} contract so aligners can be written
 * against a common gap-model abstraction.
 */
public final class AffineGapCost implements GapCost {
  private final int alpha;
  private final int beta;

  /**
   * Creates an affine gap cost function.
   *
   * @param alpha gap opening penalty (must be non-negative)
   * @param beta  gap extension penalty (must be non-negative)
   * @throws IllegalArgumentException if alpha or beta is negative
   */
  public AffineGapCost(int alpha, int beta) {
    if (alpha < 0) {
      throw new IllegalArgumentException("Gap opening penalty (alpha) must be non-negative, got: " + alpha);
    }
    if (beta < 0) {
      throw new IllegalArgumentException("Gap extension penalty (beta) must be non-negative, got: " + beta);
    }
    this.alpha = alpha;
    this.beta = beta;
  }

  /**
   * Computes the affine penalty for a gap of length {@code length}.
   *
   * @param length contiguous gap length
   * @return {@code 0} when {@code length == 0}; otherwise {@code alpha + beta * length}
   * @throws IllegalArgumentException if {@code length} is negative
   */
  @Override
  public int cost(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("Gap length must be non-negative, got: " + length);
    }
    if (length == 0) {
      return 0;
    }
    return alpha + beta * length;
  }

  /** Returns the gap opening penalty. */
  public int getAlpha() {
    return alpha;
  }

  /** Returns the gap extension penalty. */
  public int getBeta() {
    return beta;
  }
}
