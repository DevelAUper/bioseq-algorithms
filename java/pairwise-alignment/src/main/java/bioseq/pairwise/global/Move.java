package bioseq.pairwise.global;

/**
 * Traceback move for the global-alignment dynamic-programming matrix.
 *
 * <p>Each value identifies which predecessor cell was chosen when computing an optimal cost.
 */
public enum Move {
  /** Diagonal predecessor ({@code i-1, j-1}): align one residue from each sequence. */
  DIAG,
  /** Up predecessor ({@code i-1, j}): align a residue from sequence 1 against a gap. */
  UP,
  /** Left predecessor ({@code i, j-1}): align a residue from sequence 2 against a gap. */
  LEFT
}
