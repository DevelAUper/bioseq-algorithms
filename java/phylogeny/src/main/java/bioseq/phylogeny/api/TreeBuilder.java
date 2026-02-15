package bioseq.phylogeny.api;

import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;

/**
 * Contract for phylogenetic tree reconstruction algorithms.
 *
 * <p>Implementations consume a taxa distance matrix and produce a tree representing inferred
 * evolutionary relationships.
 */
public interface TreeBuilder {
  /**
   * Builds a phylogenetic tree from pairwise taxon distances.
   *
   * @param distances symmetric distance matrix between taxa
   * @return reconstructed phylogenetic tree
   */
  PhylogeneticTree buildTree(DistanceMatrix distances);
}
