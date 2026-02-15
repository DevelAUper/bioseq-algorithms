package bioseq.phylogeny.algorithm;

import bioseq.phylogeny.api.TreeBuilder;
import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UPGMA (Unweighted Pair Group Method with Arithmetic Mean) tree builder.
 *
 * <p>UPGMA repeatedly merges the two closest clusters and updates distances using cluster-size
 * weighted arithmetic means. It assumes an ultrametric model (molecular clock), so all leaves end
 * at the same root-to-leaf height in the reconstructed rooted tree.
 */
public final class UPGMA implements TreeBuilder {
  /**
   * Reconstructs a rooted phylogenetic tree from a symmetric distance matrix.
   *
   * <p>Algorithm steps:
   * <ol>
   *   <li>Initialize each taxon as a singleton cluster.</li>
   *   <li>Find the closest cluster pair.</li>
   *   <li>Merge the pair and assign child branch lengths from cluster heights.</li>
   *   <li>Update distances by weighted average of cluster sizes.</li>
   *   <li>Repeat until one cluster remains.</li>
   * </ol>
   *
   * @param distances symmetric taxon distance matrix
   * @return rooted UPGMA tree
   * @throws NullPointerException if {@code distances} is {@code null}
   */
  @Override
  public PhylogeneticTree buildTree(DistanceMatrix distances) {
    Objects.requireNonNull(distances, "distances must not be null");

    int n = distances.size();
    String[] taxa = distances.taxaNames();
    if (n == 1) {
      PhylogeneticTree.Node leaf = new PhylogeneticTree.Node(taxa[0], 0.0, null, null, true);
      return new PhylogeneticTree(leaf);
    }

    List<Cluster> clusters = new ArrayList<>(n);
    for (String taxon : taxa) {
      PhylogeneticTree.Node leaf = new PhylogeneticTree.Node(taxon, 0.0, null, null, true);
      clusters.add(new Cluster(leaf, 1, 0.0));
    }

    double[][] currentDistances = toDenseArray(distances);

    // Iteratively merge nearest clusters until one rooted cluster remains.
    while (clusters.size() > 1) {
      int clusterCount = clusters.size();

      // Step 1-2: locate the closest pair in the current distance matrix.
      int bestI = -1;
      int bestJ = -1;
      double bestDistance = Double.POSITIVE_INFINITY;
      for (int i = 0; i < clusterCount; i++) {
        for (int j = i + 1; j < clusterCount; j++) {
          double candidate = currentDistances[i][j];
          if (candidate < bestDistance) {
            bestDistance = candidate;
            bestI = i;
            bestJ = j;
          }
        }
      }

      Cluster leftCluster = clusters.get(bestI);
      Cluster rightCluster = clusters.get(bestJ);

      // Step 3: create merged cluster at height d(i,j)/2 and set child edge lengths.
      double mergedHeight = bestDistance / 2.0;
      double leftBranch = Math.max(0.0, mergedHeight - leftCluster.height());
      double rightBranch = Math.max(0.0, mergedHeight - rightCluster.height());

      PhylogeneticTree.Node leftChild = withBranchLength(leftCluster.node(), leftBranch);
      PhylogeneticTree.Node rightChild = withBranchLength(rightCluster.node(), rightBranch);
      PhylogeneticTree.Node mergedNode =
          new PhylogeneticTree.Node(null, 0.0, leftChild, rightChild, false);
      Cluster mergedCluster = new Cluster(
          mergedNode,
          leftCluster.size() + rightCluster.size(),
          mergedHeight);

      // Step 4: rebuild cluster list and distance matrix after replacing i/j by merged cluster.
      List<Integer> survivorOldIndices = new ArrayList<>();
      List<Cluster> nextClusters = new ArrayList<>(clusterCount - 1);
      for (int idx = 0; idx < clusterCount; idx++) {
        if (idx == bestI || idx == bestJ) {
          continue;
        }
        survivorOldIndices.add(idx);
        nextClusters.add(clusters.get(idx));
      }
      nextClusters.add(mergedCluster);

      int nextSize = nextClusters.size();
      double[][] nextDistances = new double[nextSize][nextSize];

      // Copy survivor-survivor distances unchanged.
      for (int a = 0; a < survivorOldIndices.size(); a++) {
        int oldA = survivorOldIndices.get(a);
        for (int b = a + 1; b < survivorOldIndices.size(); b++) {
          int oldB = survivorOldIndices.get(b);
          double value = currentDistances[oldA][oldB];
          nextDistances[a][b] = value;
          nextDistances[b][a] = value;
        }
      }

      // Weighted-average update for distances from merged cluster to each survivor.
      int mergedIndex = nextSize - 1;
      for (int a = 0; a < survivorOldIndices.size(); a++) {
        int oldA = survivorOldIndices.get(a);
        double mergedDistance =
            (leftCluster.size() * currentDistances[bestI][oldA]
                + rightCluster.size() * currentDistances[bestJ][oldA])
                / (double) mergedCluster.size();
        nextDistances[a][mergedIndex] = mergedDistance;
        nextDistances[mergedIndex][a] = mergedDistance;
      }
      nextDistances[mergedIndex][mergedIndex] = 0.0;

      clusters = nextClusters;
      currentDistances = nextDistances;
    }

    return new PhylogeneticTree(clusters.get(0).node());
  }

  /** Converts immutable matrix API to a dense mutable array for iterative clustering. */
  private static double[][] toDenseArray(DistanceMatrix distances) {
    int n = distances.size();
    double[][] dense = new double[n][n];
    for (int i = 0; i < n; i++) {
      for (int j = 0; j < n; j++) {
        dense[i][j] = distances.distance(i, j);
      }
    }
    return dense;
  }

  /** Returns a node copy with identical topology but a new parent-edge branch length. */
  private static PhylogeneticTree.Node withBranchLength(PhylogeneticTree.Node node, double branchLength) {
    return new PhylogeneticTree.Node(
        node.label(),
        branchLength,
        node.left(),
        node.right(),
        node.isLeaf());
  }

  /** Cluster state tracked during UPGMA agglomeration. */
  private record Cluster(PhylogeneticTree.Node node, int size, double height) {
  }
}
