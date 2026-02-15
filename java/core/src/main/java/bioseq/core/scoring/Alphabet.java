package bioseq.core.scoring;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Residue alphabet used to validate sequences and index score matrices.
 *
 * <p>All symbols are normalized to uppercase at construction time, and all lookups/validation
 * also normalize inputs to uppercase. This guarantees case-insensitive behavior throughout the
 * scoring layer.
 */
public final class Alphabet {
  private final Set<Character> symbols;

  /**
   * Creates an alphabet from a symbol collection.
   *
   * @param symbols symbols allowed by this alphabet
   * @throws NullPointerException if {@code symbols} is {@code null}
   * @throws IllegalArgumentException if {@code symbols} is empty, contains {@code null}, or
   *     contains duplicates after uppercase normalization
   */
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

  /**
   * Convenience factory for creating an alphabet from an ordered symbol list.
   *
   * @param symbols symbols allowed by this alphabet
   * @return uppercase-normalized alphabet
   * @throws NullPointerException if {@code symbols} is {@code null}
   * @throws IllegalArgumentException if the symbols are invalid
   */
  public static Alphabet fromSymbols(List<Character> symbols) {
    return new Alphabet(symbols);
  }

  /**
   * Tests whether a residue symbol is in this alphabet.
   *
   * @param c residue symbol to test
   * @return {@code true} if the normalized symbol exists in the alphabet
   */
  public boolean contains(char c) {
    return symbols.contains(Character.toUpperCase(c));
  }

  /**
   * Validates that every residue in the input string belongs to this alphabet.
   *
   * @param residues residue string to validate
   * @throws NullPointerException if {@code residues} is {@code null}
   * @throws IllegalArgumentException if any residue is not part of this alphabet
   */
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

  /**
   * Returns an unmodifiable view of the normalized alphabet symbols.
   *
   * <p>The iteration order matches insertion order of the original symbol collection.
   *
   * @return immutable symbol set
   */
  public Set<Character> symbols() {
    return Collections.unmodifiableSet(symbols);
  }
}
