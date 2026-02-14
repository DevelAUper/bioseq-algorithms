package bioseq.cli;

/**
 * CLI entry point for counting optimal global alignments.
 */
public final class GlobalCountApp {
  private GlobalCountApp() {
  }

  public static void main(String[] args) {
    String[] forwarded = new String[args.length + 1];
    forwarded[0] = "global_count";
    System.arraycopy(args, 0, forwarded, 1, args.length);
    BioseqCli.main(forwarded);
  }
}
