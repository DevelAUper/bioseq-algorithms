package bioseq.pairwise.model;

/**
 * Result of an alignment: optimal score and aligned strings.
 */
public final class AlignmentResult {
  private final int score;
  private final String alignedA;
  private final String alignedB;

  public AlignmentResult(int score, String alignedA, String alignedB) {
    this.score = score;
    this.alignedA = alignedA;
    this.alignedB = alignedB;
  }

  public int getScore() {
    return score;
  }

  public String getAlignedA() {
    return alignedA;
  }

  public String getAlignedB() {
    return alignedB;
  }
}
