package bioseq.cli;

/**
 * CLI entry point for global alignment with linear gap penalties.
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
