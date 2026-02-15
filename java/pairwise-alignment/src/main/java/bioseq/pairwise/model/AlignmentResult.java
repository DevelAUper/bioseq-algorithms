package bioseq.pairwise.model;

import java.util.Objects;

/**
 * Immutable container for the output of a pairwise alignment run.
 *
 * <p>Stores the optimal alignment cost and one aligned sequence pair with gaps. This value object
 * is returned by aligners and then formatted by CLI/application layers.
 */
public final class AlignmentResult {
  private final int cost;
  private final String aligned1;
  private final String aligned2;

  /**
   * Creates an alignment result.
   *
   * @param cost optimal total alignment cost
   * @param aligned1 aligned representation of sequence 1 (including gap characters)
   * @param aligned2 aligned representation of sequence 2 (including gap characters)
   * @throws NullPointerException if {@code aligned1} or {@code aligned2} is {@code null}
   */
  public AlignmentResult(int cost, String aligned1, String aligned2) {
    this.cost = cost;
    this.aligned1 = Objects.requireNonNull(aligned1, "aligned1 must not be null");
    this.aligned2 = Objects.requireNonNull(aligned2, "aligned2 must not be null");
  }

  /**
   * Returns the optimal alignment cost.
   *
   * @return minimum cost
   */
  public int getCost() {
    return cost;
  }

  /**
   * Returns aligned sequence 1.
   *
   * @return alignment string for input sequence 1
   */
  public String getAligned1() {
    return aligned1;
  }

  /**
   * Returns aligned sequence 2.
   *
   * @return alignment string for input sequence 2
   */
  public String getAligned2() {
    return aligned2;
  }

  /**
   * Backward-compatible alias for {@link #getCost()}.
   *
   * @return minimum cost
   */
  public int getScore() {
    return getCost();
  }

  /**
   * Backward-compatible alias for {@link #getAligned1()}.
   *
   * @return alignment string for input sequence 1
   */
  public String getAlignedA() {
    return getAligned1();
  }

  /**
   * Backward-compatible alias for {@link #getAligned2()}.
   *
   * @return alignment string for input sequence 2
   */
  public String getAlignedB() {
    return getAligned2();
  }
}
