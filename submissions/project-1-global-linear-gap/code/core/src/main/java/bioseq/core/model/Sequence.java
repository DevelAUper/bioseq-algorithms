package bioseq.core.model;

import java.util.Objects;

/**
 * Immutable biological sequence with an identifier and residue string.
 * <p>Residues may be empty.
 */
public final class Sequence {
  private final String id;
  private final String residues;

  public Sequence(String id, String residues) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.residues = Objects.requireNonNull(residues, "residues must not be null");
  }

  /**
   * Creates a sequence with default id {@code seq}.
   */
  public static Sequence of(String residues) {
    return new Sequence("seq", residues);
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
