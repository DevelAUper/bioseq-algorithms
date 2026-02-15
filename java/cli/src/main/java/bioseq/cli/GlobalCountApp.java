package bioseq.cli;

/**
 * Backward-compatible CLI entry point for counting optimal min-cost global alignments.
 *
 * <p>This forwards to {@code bioseq-cli global_count ...}. The command accepts either
 * {@code --seq1/--seq2} (direct sequence strings) or {@code --fasta1/--fasta2} (first FASTA record in each
 * file), plus {@code --matrix} and {@code --gap}.
 *
 * <p>The matrix is a distance/cost matrix: lower values are better, and both cost and count are computed
 * under minimization.
 *
 * <p>Output:
 * <ul>
 *   <li>{@code cost: <int>}</li>
 *   <li>{@code count: <BigInteger>}</li>
 * </ul>
 */
public final class GlobalCountApp {
  /** Utility launcher class; not instantiable. */
  private GlobalCountApp() {
  }

  /**
   * Compatibility entry point that forwards arguments to {@code bioseq-cli global_count}.
   *
   * @param args raw command-line arguments for the legacy command
   */
  public static void main(String[] args) {
    String[] forwarded = new String[args.length + 1];
    forwarded[0] = "global_count";
    System.arraycopy(args, 0, forwarded, 1, args.length);
    BioseqCli.main(forwarded);
  }
}
