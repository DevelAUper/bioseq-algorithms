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
 * Distance/cost matrix for pairwise residue comparisons.
 *
 * <p>This class stores:
 * <ul>
 *   <li>an {@link Alphabet} describing valid residue symbols</li>
 *   <li>a dense {@code n x n} integer matrix of pairwise costs</li>
 *   <li>a symbol-to-index map used for constant-time lookups</li>
 * </ul>
 *
 * <p>Lower values represent better alignments in this codebase (min-cost dynamic programming).
 */
public final class ScoreMatrix {
  private final Alphabet alphabet;
  private final int[][] values;
  private final Map<Character, Integer> indexBySymbol;

  /** Internal constructor used by parser factories. */
  private ScoreMatrix(Alphabet alphabet, int[][] values, Map<Character, Integer> indexBySymbol) {
    this.alphabet = alphabet;
    this.values = values;
    this.indexBySymbol = indexBySymbol;
  }

  /**
   * Returns the matrix cost for a residue pair.
   *
   * @param a residue from sequence A
   * @param b residue from sequence B
   * @return lookup value from the parsed matrix
   * @throws IllegalArgumentException if either symbol is not in the alphabet
   */
  public int cost(char a, char b) {
    int i = indexOf(a);
    int j = indexOf(b);
    return values[i][j];
  }

  /**
   * Returns the alphabet that defines valid symbols for this matrix.
   *
   * @return matrix alphabet
   */
  public Alphabet alphabet() {
    return alphabet;
  }

  /**
   * Backward-compatible alias for {@link #cost(char, char)}.
   *
   * @param a residue from sequence A
   * @param b residue from sequence B
   * @return lookup value from the parsed matrix
   * @throws IllegalArgumentException if either symbol is not in the alphabet
   */
  public int score(char a, char b) {
    return cost(a, b);
  }

  /**
   * Parses a PHYLIP-like square matrix file.
   *
   * <p>Expected format:
   * <ul>
   *   <li>First non-empty line: matrix size {@code n}</li>
   *   <li>Next {@code n} non-empty lines: {@code <symbol> <n integers>}</li>
   * </ul>
   *
   * <p>Symbols are normalized to uppercase. Rows and columns are indexed by row symbol order.
   *
   * @param path path to matrix file
   * @return parsed score matrix
   * @throws NullPointerException if {@code path} is {@code null}
   * @throws IllegalArgumentException if the file content is malformed
   * @throws UncheckedIOException if the file cannot be read
   */
  public static ScoreMatrix fromPhylipLikeFile(Path path) {
    Objects.requireNonNull(path, "path must not be null");
    final List<String> lines;
    try {
      lines = Files.readAllLines(path);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read score matrix file: " + path, e);
    }

    // Ignore blank lines to allow loosely formatted matrix files.
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

    // First non-empty line declares square matrix size.
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

      // Row key symbol defines both the alphabet entry and this row index.
      char symbol = Character.toUpperCase(tokens[0].charAt(0));
      if (indexBySymbol.containsKey(symbol)) {
        throw new IllegalArgumentException("Duplicate symbol '" + symbol + "' in matrix");
      }

      symbols.add(symbol);
      indexBySymbol.put(symbol, row);

      // Parse the full numeric row in column order.
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

  /**
   * Parses a matrix and optionally checks it against a provided alphabet.
   *
   * @param path path to matrix file
   * @param alphabet optional expected alphabet; when non-null it must match exactly
   * @return parsed score matrix
   * @throws IOException declared for compatibility with previous API versions
   * @throws IllegalArgumentException if matrix content is invalid or alphabet mismatch occurs
   */
  public static ScoreMatrix fromFile(Path path, Alphabet alphabet) throws IOException {
    ScoreMatrix parsed = fromPhylipLikeFile(path);
    if (alphabet != null && !parsed.alphabet().symbols().equals(alphabet.symbols())) {
      throw new IllegalArgumentException("Provided alphabet does not match matrix symbols");
    }
    return parsed;
  }

  /**
   * Resolves a residue symbol to its matrix index using uppercase normalization.
   */
  private int indexOf(char symbol) {
    char normalized = Character.toUpperCase(symbol);
    Integer index = indexBySymbol.get(normalized);
    if (index == null) {
      throw new IllegalArgumentException("Unknown residue symbol: '" + normalized + "'");
    }
    return index;
  }
}
