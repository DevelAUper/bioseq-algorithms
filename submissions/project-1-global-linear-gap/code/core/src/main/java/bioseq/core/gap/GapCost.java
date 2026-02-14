package bioseq.core.gap;

/**
 * Gap cost function for insertions and deletions.
 */
public interface GapCost {
  int cost(int length);
}
