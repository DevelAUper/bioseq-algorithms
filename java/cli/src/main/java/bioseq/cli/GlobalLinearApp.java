package bioseq.cli;

/**
 * Backward-compatible CLI entry point for min-cost global alignment with a linear gap penalty.
 *
 * <p>This forwards to {@code bioseq-cli global_linear ...}. The command accepts either
 * {@code --seq1/--seq2} (direct sequence strings) or {@code --fasta1/--fasta2} (first FASTA record in each
 * file), plus {@code --matrix} and {@code --gap}.
 *
 * <p>The matrix is a distance/cost matrix: lower values are better, and the algorithm minimizes total cost.
 *
 * <p>Output:
 * <ul>
 *   <li>Without {@code --traceback}: prints only the optimal cost as an integer.</li>
 *   <li>With {@code --traceback}: prints {@code cost: ...} and one optimal aligned pair.</li>
 * </ul>
 */
public final class GlobalLinearApp {
  private GlobalLinearApp() {
  }

  public static void main(String[] args) {
    String[] forwarded = new String[args.length + 1];
    forwarded[0] = "global_linear";
    System.arraycopy(args, 0, forwarded, 1, args.length);
    BioseqCli.main(forwarded);
  }
}
