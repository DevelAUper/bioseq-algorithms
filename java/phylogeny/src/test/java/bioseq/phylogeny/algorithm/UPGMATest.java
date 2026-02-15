package bioseq.phylogeny.algorithm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;
import org.junit.jupiter.api.Test;

class UPGMATest {
  @Test
  void buildsThreeTaxonTreeAndSerializesToNewick() {
    DistanceMatrix distances = new DistanceMatrix(
        new String[] {"A", "B", "C"},
        new double[][] {
            {0.0, 2.0, 4.0},
            {2.0, 0.0, 4.0},
            {4.0, 4.0, 0.0}
        });

    PhylogeneticTree tree = new UPGMA().buildTree(distances);
    String newick = tree.toNewick();

    assertEquals(3, leafCount(tree.root()));
    assertTrue(newick.endsWith(";"));
    assertTrue(newick.contains("A"));
    assertTrue(newick.contains("B"));
    assertTrue(newick.contains("C"));
  }

  private static int leafCount(PhylogeneticTree.Node node) {
    if (node.isLeaf()) {
      return 1;
    }
    return leafCount(node.left()) + leafCount(node.right());
  }
}
