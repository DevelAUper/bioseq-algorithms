package bioseq.core.scoring;

/**
 * Alphabet for sequence residues.
 */
public final class Alphabet {
  private final String symbols;

  public Alphabet(String symbols) {
    this.symbols = symbols;
  }

  public String getSymbols() {
    return symbols;
  }
}
