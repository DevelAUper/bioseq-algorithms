package bioseq.cli;

/**
 * Backward-compatible CLI entry point for min-cost global alignment with affine gap penalties.
 *
 * <p>This forwards to {@code bioseq-cli global_affine ...}. The command accepts either
 * {@code --seq1/--seq2} (direct sequence strings) or {@code --fasta1/--fasta2}
 * (first FASTA record in each file), plus {@code --matrix}, {@code --alpha}, and {@code --beta}.
 *
 * <p>Output always includes alignment cost and number of optimal alignments.
 * With {@code --traceback}, one optimal aligned pair is additionally emitted in FASTA-like form.
 */
public final class GlobalAffineApp {
  /** Utility launcher class; not instantiable. */
  private GlobalAffineApp() {
  }

  /**
   * Compatibility entry point that forwards arguments to {@code bioseq-cli global_affine}.
   *
   * @param args raw command-line arguments for the legacy command
   */
  public static void main(String[] args) {
    String[] forwarded = new String[args.length + 1];
    forwarded[0] = "global_affine";
    System.arraycopy(args, 0, forwarded, 1, args.length);
    BioseqCli.main(forwarded);
  }
}
