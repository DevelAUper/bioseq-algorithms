package bioseq.core.model;

/**
 * Immutable biological sequence with an identifier and residue string.
 */
public final class Sequence {
  private final String id;
  private final String residues;

  public Sequence(String id, String residues) {
    this.id = id;
    this.residues = residues;
  }

  public String getId() {
    return id;
  }

  public String getResidues() {
    return residues;
  }

  public int length() {
    return residues.length();
  }

  @Override
  public String toString() {
    return id + ":" + residues;
  }
}
