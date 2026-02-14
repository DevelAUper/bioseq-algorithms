package bioseq.pairwise.model;

import java.util.Objects;

/**
 * Immutable result of a min-cost pairwise alignment.
 */
public final class AlignmentResult {
  private final int cost;
  private final String aligned1;
  private final String aligned2;

  public AlignmentResult(int cost, String aligned1, String aligned2) {
    this.cost = cost;
    this.aligned1 = Objects.requireNonNull(aligned1, "aligned1 must not be null");
    this.aligned2 = Objects.requireNonNull(aligned2, "aligned2 must not be null");
  }

  public int getCost() {
    return cost;
  }

  public String getAligned1() {
    return aligned1;
  }

  public String getAligned2() {
    return aligned2;
  }

  /**
   * Backward-compatible alias for {@link #getCost()}.
   */
  public int getScore() {
    return getCost();
  }

  /**
   * Backward-compatible alias for {@link #getAligned1()}.
   */
  public String getAlignedA() {
    return getAligned1();
  }

  /**
   * Backward-compatible alias for {@link #getAligned2()}.
   */
  public String getAlignedB() {
    return getAligned2();
  }
}
