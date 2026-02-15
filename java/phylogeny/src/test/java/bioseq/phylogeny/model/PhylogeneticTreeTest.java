package bioseq.phylogeny.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PhylogeneticTreeTest {
  @Test
  void serializesSimpleTreeToNewick() {
    PhylogeneticTree.Node a = new PhylogeneticTree.Node("A", 1.0, null, null, true);
    PhylogeneticTree.Node b = new PhylogeneticTree.Node("B", 2.0, null, null, true);
    PhylogeneticTree.Node c = new PhylogeneticTree.Node("C", 3.0, null, null, true);

    PhylogeneticTree.Node right = new PhylogeneticTree.Node(null, 0.5, b, c, false);
    PhylogeneticTree.Node root = new PhylogeneticTree.Node(null, 0.0, a, right, false);

    PhylogeneticTree tree = new PhylogeneticTree(root);
    assertEquals("(A:1,(B:2,C:3):0.5);", tree.toNewick());
  }
}
