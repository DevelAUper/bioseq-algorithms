package bioseq.phylogeny.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class DistanceMatrixTest {
  @Test
  void constructsAndSupportsDistanceLookup() {
    DistanceMatrix distances = new DistanceMatrix(
        new String[] {"A", "B", "C"},
        new double[][] {
            {0.0, 1.0, 2.0},
            {1.0, 0.0, 3.0},
            {2.0, 3.0, 0.0}
        });

    assertEquals(3, distances.size());
    assertEquals(1.0, distances.distance(0, 1));
    assertEquals(3.0, distances.distance("B", "C"));

    String[] copy = distances.taxaNames();
    copy[0] = "X";
    assertArrayEquals(new String[] {"A", "B", "C"}, distances.taxaNames());
  }

  @Test
  void rejectsNonSymmetricMatrices() {
    assertThrows(IllegalArgumentException.class, () -> new DistanceMatrix(
        new String[] {"A", "B"},
        new double[][] {
            {0.0, 1.0},
            {2.0, 0.0}
        }));
  }

  @Test
  void reduceRemovesOneTaxonAndReturnsNewMatrix() {
    DistanceMatrix distances = new DistanceMatrix(
        new String[] {"A", "B", "C"},
        new double[][] {
            {0.0, 1.0, 2.0},
            {1.0, 0.0, 3.0},
            {2.0, 3.0, 0.0}
        });

    DistanceMatrix reduced = distances.reduce(1);
    assertEquals(2, reduced.size());
    assertArrayEquals(new String[] {"A", "C"}, reduced.taxaNames());
    assertEquals(2.0, reduced.distance(0, 1));
    assertEquals(2.0, reduced.distance("A", "C"));
  }
}
