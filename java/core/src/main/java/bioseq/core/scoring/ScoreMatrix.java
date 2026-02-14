package bioseq.core.scoring;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Distance-based scoring matrix for residue pairs.
 */
public final class ScoreMatrix {
  private final Alphabet alphabet;
  private final int[][] values;
  private final Map<Character, Integer> indexBySymbol;

  private ScoreMatrix(Alphabet alphabet, int[][] values, Map<Character, Integer> indexBySymbol) {
    this.alphabet = alphabet;
    this.values = values;
    this.indexBySymbol = indexBySymbol;
  }

  public int cost(char a, char b) {
    int i = indexOf(a);
    int j = indexOf(b);
    return values[i][j];
  }

  public Alphabet alphabet() {
    return alphabet;
  }

  public int score(char a, char b) {
    return cost(a, b);
  }

  public static ScoreMatrix fromPhylipLikeFile(Path path) {
    Objects.requireNonNull(path, "path must not be null");
    final List<String> lines;
    try {
      lines = Files.readAllLines(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read score matrix file: " + path, e);
    }

    List<String> nonEmpty = new ArrayList<>();
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty()) {
        nonEmpty.add(trimmed);
      }
    }

    if (nonEmpty.isEmpty()) {
      throw new IllegalArgumentException("Score matrix file is empty: " + path);
    }

    final int n;
    try {
      n = Integer.parseInt(nonEmpty.get(0));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(
          "First non-empty line must be an integer matrix size, got: '" + nonEmpty.get(0) + "'", e);
    }

    if (n <= 0) {
      throw new IllegalArgumentException("Matrix size must be positive, got: " + n);
    }

    if (nonEmpty.size() - 1 < n) {
      throw new IllegalArgumentException("Expected " + n + " matrix rows, found " + (nonEmpty.size() - 1));
    }

    List<Character> symbols = new ArrayList<>(n);
    Map<Character, Integer> indexBySymbol = new LinkedHashMap<>();
    int[][] values = new int[n][n];

    for (int row = 0; row < n; row++) {
      String raw = nonEmpty.get(row + 1);
      String[] tokens = raw.split("\\s+");
      if (tokens.length != n + 1) {
        throw new IllegalArgumentException(
            "Invalid row " + (row + 1) + ": expected symbol + " + n + " integers, got " + tokens.length);
      }

      if (tokens[0].length() != 1) {
        throw new IllegalArgumentException(
            "Invalid symbol in row " + (row + 1) + ": '" + tokens[0] + "' (must be a single character)");
      }

      char symbol = Character.toUpperCase(tokens[0].charAt(0));
      if (indexBySymbol.containsKey(symbol)) {
        throw new IllegalArgumentException("Duplicate symbol '" + symbol + "' in matrix");
      }

      symbols.add(symbol);
      indexBySymbol.put(symbol, row);

      for (int col = 0; col < n; col++) {
        try {
          values[row][col] = Integer.parseInt(tokens[col + 1]);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(
              "Invalid integer at row " + (row + 1) + ", column " + (col + 1) + ": '" + tokens[col + 1] + "'", e);
        }
      }
    }

    return new ScoreMatrix(Alphabet.fromSymbols(symbols), values, indexBySymbol);
  }

  public static ScoreMatrix fromFile(Path path, Alphabet alphabet) throws IOException {
    ScoreMatrix parsed = fromPhylipLikeFile(path);
    if (alphabet != null && !parsed.alphabet().symbols().equals(alphabet.symbols())) {
      throw new IllegalArgumentException("Provided alphabet does not match matrix symbols");
    }
    return parsed;
  }

  private int indexOf(char symbol) {
    char normalized = Character.toUpperCase(symbol);
    Integer index = indexBySymbol.get(normalized);
    if (index == null) {
      throw new IllegalArgumentException("Unknown residue symbol: '" + normalized + "'");
    }
    return index;
  }
}
