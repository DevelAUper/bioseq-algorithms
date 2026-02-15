package bioseq.phylogeny.algorithm;

import bioseq.phylogeny.api.TreeBuilder;
import bioseq.phylogeny.model.DistanceMatrix;
import bioseq.phylogeny.model.PhylogeneticTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Neighbor-Joining phylogenetic tree builder.
 *
 * <p>Neighbor-Joining iteratively joins the pair minimizing the Q-criterion, then updates a
 * reduced distance matrix with a new internal node. Unlike UPGMA, it does not assume a strict
 * molecular clock and is therefore suitable for non-ultrametric distances.
 */
public final class NeighborJoining implements TreeBuilder {
  /**
   * Builds a tree from pairwise distances using the Neighbor-Joining algorithm.
   *
   * @param distances symmetric taxon distance matrix
   * @return reconstructed tree in rooted binary representation
   * @throws NullPointerException if {@code distances} is {@code null}
   */
  @Override
  public PhylogeneticTree buildTree(DistanceMatrix distances) {
    Objects.requireNonNull(distances, "distances must not be null");

    int n = distances.size();
    String[] taxa = distances.taxaNames();
    if (n == 1) {
      return new PhylogeneticTree(new PhylogeneticTree.Node(taxa[0], 0.0, null, null, true));
    }

    List<PhylogeneticTree.Node> clusters = new ArrayList<>(n);
    for (String taxon : taxa) {
      clusters.add(new PhylogeneticTree.Node(taxon, 0.0, null, null, true));
    }

    double[][] currentDistances = toDenseArray(distances);

    // Repeatedly join the best pair until two clusters remain.
    while (clusters.size() > 2) {
      int clusterCount = clusters.size();

      // Step 1: precompute row sums used in the Q-matrix and limb lengths.
      double[] rowSums = new double[clusterCount];
      for (int i = 0; i < clusterCount; i++) {
        double sum = 0.0;
        for (int k = 0; k < clusterCount; k++) {
          if (k != i) {
            sum += currentDistances[i][k];
          }
        }
        rowSums[i] = sum;
      }

      // Step 2: choose pair (i, j) minimizing Q(i,j).
      int bestI = -1;
      int bestJ = -1;
      double bestQ = Double.POSITIVE_INFINITY;
      for (int i = 0; i < clusterCount; i++) {
        for (int j = i + 1; j < clusterCount; j++) {
          double qValue = (clusterCount - 2) * currentDistances[i][j] - rowSums[i] - rowSums[j];
          if (qValue < bestQ) {
            bestQ = qValue;
            bestI = i;
            bestJ = j;
          }
        }
      }

      // Step 3: compute branch lengths from i/j to the new internal node.
      double dij = currentDistances[bestI][bestJ];
      double limbI = (dij / 2.0) + ((rowSums[bestI] - rowSums[bestJ]) / (2.0 * (clusterCount - 2)));
      double limbJ = dij - limbI;
      limbI = Math.max(0.0, limbI);
      limbJ = Math.max(0.0, limbJ);

      PhylogeneticTree.Node leftChild = withBranchLength(clusters.get(bestI), limbI);
      PhylogeneticTree.Node rightChild = withBranchLength(clusters.get(bestJ), limbJ);
      PhylogeneticTree.Node merged = new PhylogeneticTree.Node(null, 0.0, leftChild, rightChild, false);

      // Step 4: compute distances from merged node to all survivors.
      List<Integer> survivorOldIndices = new ArrayList<>();
      List<PhylogeneticTree.Node> nextClusters = new ArrayList<>(clusterCount - 1);
      for (int idx = 0; idx < clusterCount; idx++) {
        if (idx == bestI || idx == bestJ) {
          continue;
        }
        survivorOldIndices.add(idx);
        nextClusters.add(clusters.get(idx));
      }
      nextClusters.add(merged);

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

      // Neighbor-joining reduction formula for merged-to-survivor distances.
      int mergedIndex = nextSize - 1;
      for (int a = 0; a < survivorOldIndices.size(); a++) {
        int oldA = survivorOldIndices.get(a);
        double mergedDistance =
            (currentDistances[bestI][oldA] + currentDistances[bestJ][oldA] - dij) / 2.0;
        nextDistances[a][mergedIndex] = mergedDistance;
        nextDistances[mergedIndex][a] = mergedDistance;
      }
      nextDistances[mergedIndex][mergedIndex] = 0.0;

      clusters = nextClusters;
      currentDistances = nextDistances;
    }

    // Step 5: final two-node case; connect both with equal split of remaining distance.
    double finalDistance = currentDistances[0][1];
    double branchLength = Math.max(0.0, finalDistance / 2.0);
    PhylogeneticTree.Node left = withBranchLength(clusters.get(0), branchLength);
    PhylogeneticTree.Node right = withBranchLength(clusters.get(1), branchLength);
    PhylogeneticTree.Node root = new PhylogeneticTree.Node(null, 0.0, left, right, false);
    return new PhylogeneticTree(root);
  }

  /** Converts immutable matrix API to a dense mutable array for iterative reductions. */
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

  /** Returns a node copy with unchanged subtree and replaced parent-edge branch length. */
  private static PhylogeneticTree.Node withBranchLength(PhylogeneticTree.Node node, double branchLength) {
    return new PhylogeneticTree.Node(
        node.label(),
        branchLength,
        node.left(),
        node.right(),
        node.isLeaf());
  }
}
