package bioseq.phylogeny.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable symmetric distance matrix for phylogenetic taxa.
 *
 * <p>Rows/columns correspond to taxa in {@code taxaNames()} order. Distances must be symmetric,
 * finite, and non-negative with zero diagonal.
 */
public final class DistanceMatrix {
  private static final double EPS = 1e-9;

  private final String[] taxaNames;
  private final double[][] distances;
  private final Map<String, Integer> indexByTaxon;

  /**
   * Creates a validated immutable distance matrix.
   *
   * @param taxaNames taxon names in matrix order
   * @param distances square symmetric distance matrix
   * @throws NullPointerException if any argument is {@code null}
   * @throws IllegalArgumentException if matrix is invalid (shape, symmetry, diagonal, negativity)
   */
  public DistanceMatrix(String[] taxaNames, double[][] distances) {
    Objects.requireNonNull(taxaNames, "taxaNames must not be null");
    Objects.requireNonNull(distances, "distances must not be null");
    if (taxaNames.length == 0) {
      throw new IllegalArgumentException("taxaNames must contain at least one entry");
    }
    if (distances.length != taxaNames.length) {
      throw new IllegalArgumentException(
          "distances row count must equal taxaNames length, got "
              + distances.length + " and " + taxaNames.length);
    }

    this.indexByTaxon = new LinkedHashMap<>();
    this.taxaNames = new String[taxaNames.length];
    for (int i = 0; i < taxaNames.length; i++) {
      String taxon = Objects.requireNonNull(taxaNames[i], "taxon name at index " + i + " must not be null");
      if (taxon.isBlank()) {
        throw new IllegalArgumentException("taxon name at index " + i + " must not be blank");
      }
      if (indexByTaxon.putIfAbsent(taxon, i) != null) {
        throw new IllegalArgumentException("duplicate taxon name: " + taxon);
      }
      this.taxaNames[i] = taxon;
    }

    int n = taxaNames.length;
    this.distances = new double[n][n];
    for (int i = 0; i < n; i++) {
      double[] row = Objects.requireNonNull(distances[i], "distance row " + i + " must not be null");
      if (row.length != n) {
        throw new IllegalArgumentException(
            "distance row " + i + " length must be " + n + ", got " + row.length);
      }
      for (int j = 0; j < n; j++) {
        double value = row[j];
        if (!Double.isFinite(value)) {
          throw new IllegalArgumentException("distance at (" + i + "," + j + ") must be finite");
        }
        if (value < 0.0) {
          throw new IllegalArgumentException("distance at (" + i + "," + j + ") must be non-negative");
        }
        if (i == j && Math.abs(value) > EPS) {
          throw new IllegalArgumentException(
              "distance diagonal must be 0 at index " + i + ", got " + value);
        }
        this.distances[i][j] = value;
      }
    }

    for (int i = 0; i < n; i++) {
      for (int j = i + 1; j < n; j++) {
        if (Math.abs(this.distances[i][j] - this.distances[j][i]) > EPS) {
          throw new IllegalArgumentException(
              "distance matrix must be symmetric at (" + i + "," + j + ") and (" + j + "," + i + ")");
        }
      }
    }
  }

  /**
   * Returns distance by row/column index.
   *
   * @param i first taxon index
   * @param j second taxon index
   * @return pairwise distance
   */
  public double distance(int i, int j) {
    if (i < 0 || i >= distances.length || j < 0 || j >= distances.length) {
      throw new IllegalArgumentException("indices out of bounds: (" + i + "," + j + ")");
    }
    return distances[i][j];
  }

  /**
   * Returns distance by taxon names.
   *
   * @param taxon1 first taxon name
   * @param taxon2 second taxon name
   * @return pairwise distance
   */
  public double distance(String taxon1, String taxon2) {
    Objects.requireNonNull(taxon1, "taxon1 must not be null");
    Objects.requireNonNull(taxon2, "taxon2 must not be null");
    Integer i = indexByTaxon.get(taxon1);
    Integer j = indexByTaxon.get(taxon2);
    if (i == null || j == null) {
      throw new IllegalArgumentException("unknown taxon name(s): " + taxon1 + ", " + taxon2);
    }
    return distances[i][j];
  }

  /**
   * Returns number of taxa.
   *
   * @return matrix dimension
   */
  public int size() {
    return taxaNames.length;
  }

  /**
   * Returns taxon names in matrix order.
   *
   * @return defensive copy of taxon names
   */
  public String[] taxaNames() {
    return Arrays.copyOf(taxaNames, taxaNames.length);
  }

  /**
   * Returns a new distance matrix with one taxon removed.
   *
   * @param taxonToRemove index of taxon to remove
   * @return reduced distance matrix
   */
  public DistanceMatrix reduce(int taxonToRemove) {
    if (taxonToRemove < 0 || taxonToRemove >= size()) {
      throw new IllegalArgumentException("taxonToRemove out of bounds: " + taxonToRemove);
    }
    if (size() == 1) {
      throw new IllegalArgumentException("cannot reduce a size-1 matrix");
    }

    int newSize = size() - 1;
    String[] newTaxa = new String[newSize];
    double[][] newDistances = new double[newSize][newSize];

    int newI = 0;
    for (int oldI = 0; oldI < size(); oldI++) {
      if (oldI == taxonToRemove) {
        continue;
      }
      newTaxa[newI] = taxaNames[oldI];
      int newJ = 0;
      for (int oldJ = 0; oldJ < size(); oldJ++) {
        if (oldJ == taxonToRemove) {
          continue;
        }
        newDistances[newI][newJ] = distances[oldI][oldJ];
        newJ++;
      }
      newI++;
    }

    return new DistanceMatrix(newTaxa, newDistances);
  }
}
