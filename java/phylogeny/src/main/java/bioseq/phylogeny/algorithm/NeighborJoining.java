package bioseq.phylogeny.algorithm;

import bioseq.phylogeny.api.TreeBuilder;
import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;

/**
 * Placeholder scaffold for Neighbor-Joining tree construction.
 *
 * <p>Neighbor-Joining iteratively selects the pair of taxa/clusters minimizing a corrected
 * divergence criterion, joins them into a new internal node, and updates distances without
 * requiring a strict molecular-clock assumption.
 */
public final class NeighborJoining implements TreeBuilder {
  @Override
  public PhylogeneticTree buildTree(DistanceMatrix distances) {
    // TODO Step 1: Compute Q-matrix (neighbor-joining criterion) from current distances.
    // TODO Step 2: Select pair (i, j) minimizing Q and compute branch lengths to new node.
    // TODO Step 3: Create merged node and update reduced distance matrix.
    // TODO Step 4: Repeat until two nodes remain, then finalize the root.
    return null;
  }
}
