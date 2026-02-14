package bioseq.core.scoring;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.Collections;

/**
 * Alphabet for sequence residues.
 */
  public final class Alphabet {
  private final Set<Character> symbols;

  public Alphabet(Collection<Character> symbols) {
    Objects.requireNonNull(symbols, "symbols must not be null");
    if (symbols.isEmpty()) {
      throw new IllegalArgumentException("Alphabet must contain at least one symbol");
    }

    this.symbols = new LinkedHashSet<>();
    for (Character symbol : symbols) {
      if (symbol == null) {
        throw new IllegalArgumentException("Alphabet symbols must not contain null");
      }
      char normalized = Character.toUpperCase(symbol);
      if (!this.symbols.add(normalized)) {
        throw new IllegalArgumentException("Duplicate alphabet symbol: '" + normalized + "'");
      }
    }
  }

  public static Alphabet fromSymbols(List<Character> symbols) {
    return new Alphabet(symbols);
  }

  public boolean contains(char c) {
    return symbols.contains(Character.toUpperCase(c));
  }

  public void validate(String residues) {
    Objects.requireNonNull(residues, "residues must not be null");
    String normalized = residues.toUpperCase(Locale.ROOT);
    for (int i = 0; i < normalized.length(); i++) {
      char residue = normalized.charAt(i);
      if (!contains(residue)) {
        throw new IllegalArgumentException(
            "Invalid residue '" + residue + "' at position " + (i + 1));
      }
    }
  }

  public Set<Character> symbols() {
    return Collections.unmodifiableSet(symbols);
  }
}
