package bioseq.phylogeny.algorithm;

import bioseq.phylogeny.api.TreeBuilder;
import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;

/**
 * Placeholder scaffold for UPGMA tree construction.
 *
 * <p>UPGMA (Unweighted Pair Group Method with Arithmetic Mean) repeatedly merges the closest
 * clusters and updates inter-cluster distances by arithmetic averaging, assuming a molecular
 * clock (ultrametric tree).
 */
public final class UPGMA implements TreeBuilder {
  @Override
  public PhylogeneticTree buildTree(DistanceMatrix distances) {
    // TODO Step 1: Start with each taxon as its own cluster.
    // TODO Step 2: Find the two closest clusters.
    // TODO Step 3: Merge them, update distance matrix.
    // TODO Step 4: Repeat until one cluster remains.
    return null;
  }
}
