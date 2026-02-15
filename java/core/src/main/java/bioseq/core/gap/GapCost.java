package bioseq.core.gap;

/**
 * Base interface for gap-penalty models used by sequence alignment algorithms.
 *
 * <p>Implementations define how much it costs to introduce a gap of a given length, for example:
 * linear ({@code k * penalty}) or affine ({@code alpha + beta * k} for {@code k >= 1}).
 *
 * <p>This abstraction lets aligners depend on a gap model contract rather than a concrete
 * implementation.
 */
public interface GapCost {
  /**
   * Returns the penalty for a gap segment of the provided length.
   *
   * <p>The expected contract is:
   * <ul>
   *   <li>{@code cost(0) == 0}</li>
   *   <li>Negative lengths are invalid and should be rejected</li>
   *   <li>Costs are non-negative and non-decreasing with length for standard models</li>
   * </ul>
   *
   * @param length length of a single contiguous gap segment
   * @return penalty for that gap length
   * @throws IllegalArgumentException if {@code length} is negative
   */
  int cost(int length);
}
