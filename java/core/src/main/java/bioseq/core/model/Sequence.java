package bioseq.core.model;

import java.util.Objects;

/**
 * Immutable biological sequence with an identifier and residue string.
 *
 * <p>This is the core value object shared across the project layers:
 * parsers create it, aligners consume it, and CLI commands pass it between components.
 * Residues may be empty.
 */
public final class Sequence {
  private final String id;
  private final String residues;

  /**
   * Creates a sequence value.
   *
   * @param id sequence identifier
   * @param residues residue string (may be empty)
   * @throws NullPointerException if {@code id} or {@code residues} is {@code null}
   */
  public Sequence(String id, String residues) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.residues = Objects.requireNonNull(residues, "residues must not be null");
  }

  /**
   * Creates a sequence with default identifier {@code seq}.
   *
   * <p>This factory is used when an input source does not provide an explicit id.
   *
   * @param residues residue string (may be empty)
   * @return immutable sequence with id {@code seq}
   * @throws NullPointerException if {@code residues} is {@code null}
   */
  public static Sequence of(String residues) {
    return new Sequence("seq", residues);
  }

  /**
   * Returns the sequence identifier.
   *
   * @return sequence id
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the raw residue string.
   *
   * @return residues as stored in this value object
   */
  public String getResidues() {
    return residues;
  }

  /**
   * Returns the number of residues.
   *
   * @return sequence length
   */
  public int length() {
    return residues.length();
  }

  /**
   * Returns a compact debug representation in the form {@code id:residues}.
   *
   * @return textual representation of this sequence
   */
  @Override
  public String toString() {
    return id + ":" + residues;
  }
}
