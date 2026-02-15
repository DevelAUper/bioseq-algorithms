package bioseq.phylogeny.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Immutable rooted binary phylogenetic tree.
 *
 * <p>The tree can be serialized to Newick format, a standard textual representation where nested
 * parentheses encode topology and optional branch lengths encode edge distances.
 */
public final class PhylogeneticTree {
  private final Node root;

  /**
   * Creates a tree with the provided root node.
   *
   * @param root tree root
   */
  public PhylogeneticTree(Node root) {
    this.root = Objects.requireNonNull(root, "root must not be null");
  }

  /**
   * Returns tree root.
   *
   * @return root node
   */
  public Node root() {
    return root;
  }

  /**
   * Serializes this tree to Newick format.
   *
   * @return Newick string ending with {@code ;}
   */
  public String toNewick() {
    return toNewick(root, true) + ";";
  }

  private static String toNewick(Node node, boolean isRoot) {
    if (node.isLeaf()) {
      String label = node.label();
      if (!isRoot) {
        return label + ":" + formatBranchLength(node.branchLength());
      }
      return label;
    }

    String left = toNewick(node.left(), false);
    String right = toNewick(node.right(), false);
    StringBuilder out = new StringBuilder();
    out.append("(").append(left).append(",").append(right).append(")");
    if (node.label() != null && !node.label().isBlank()) {
      out.append(node.label());
    }
    if (!isRoot) {
      out.append(":").append(formatBranchLength(node.branchLength()));
    }
    return out.toString();
  }

  private static String formatBranchLength(double value) {
    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
  }

  /**
   * Immutable node in a rooted binary phylogenetic tree.
   */
  public static final class Node {
    private final String label;
    private final double branchLength;
    private final Node left;
    private final Node right;
    private final boolean isLeaf;

    /**
     * Creates a tree node.
     *
     * @param label taxon label for leaves (optional for internal nodes)
     * @param branchLength branch length from parent to this node (non-negative)
     * @param left left child (required for internal nodes)
     * @param right right child (required for internal nodes)
     * @param isLeaf whether this node is a leaf
     */
    public Node(String label, double branchLength, Node left, Node right, boolean isLeaf) {
      if (branchLength < 0.0) {
        throw new IllegalArgumentException("branchLength must be non-negative, got: " + branchLength);
      }
      if (isLeaf) {
        if (label == null || label.isBlank()) {
          throw new IllegalArgumentException("leaf nodes require a non-blank label");
        }
        if (left != null || right != null) {
          throw new IllegalArgumentException("leaf nodes must not have children");
        }
      } else {
        if (left == null || right == null) {
          throw new IllegalArgumentException("internal nodes require both left and right children");
        }
      }
      this.label = label;
      this.branchLength = branchLength;
      this.left = left;
      this.right = right;
      this.isLeaf = isLeaf;
    }

    /** Returns node label (taxon for leaves, optional for internal nodes). */
    public String label() {
      return label;
    }

    /** Returns branch length from parent to this node. */
    public double branchLength() {
      return branchLength;
    }

    /** Returns left child (internal nodes only). */
    public Node left() {
      return left;
    }

    /** Returns right child (internal nodes only). */
    public Node right() {
      return right;
    }

    /** Returns whether this node is a leaf. */
    public boolean isLeaf() {
      return isLeaf;
    }
  }
}
