package bioseq.cli;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.gap.LinearGapCost;
import bioseq.core.io.FastaReader;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.api.GlobalAligner;
import bioseq.pairwise.global.GlobalAffineAligner;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.global.OptimalAffineAlignmentCounter;
import bioseq.pairwise.global.OptimalAlignmentCounter;
import bioseq.pairwise.model.AlignmentResult;
import bioseq.pairwise.parallel.WavefrontAffineAligner;
import bioseq.pairwise.parallel.WavefrontLinearAligner;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

/**
 * CLI entry point for pairwise min-cost global alignment tasks.
 *
 * <p>Architecture:
 * <ul>
 *   <li>{@link BioseqCli} is the root Picocli command and dispatches subcommands.</li>
 *   <li>{@link GlobalLinearCommand} computes cost and optional traceback output.</li>
 *   <li>{@link GlobalCountCommand} computes optimal cost and number of optimal alignments.</li>
 *   <li>{@link GlobalAffineCommand} computes affine-gap cost/count and optional traceback output.</li>
 *   <li>{@link BaseCommand} centralizes shared argument parsing and I/O handling.</li>
 *   <li>{@link AffineBaseCommand} centralizes shared affine command parsing and I/O handling.</li>
 * </ul>
 */
@Command(
    name = "bioseq-cli",
    mixinStandardHelpOptions = true,
    subcommands = {
        BioseqCli.GlobalLinearCommand.class,
        BioseqCli.GlobalCountCommand.class,
        BioseqCli.GlobalAffineCommand.class
    })
public final class BioseqCli implements Runnable {
  /** Utility-style root command holder; instantiation is managed internally. */
  private BioseqCli() {
  }

  /**
   * CLI program entry point.
   *
   * @param args raw command-line arguments
   */
  public static void main(String[] args) {
    int exitCode = new CommandLine(new BioseqCli()).execute(args);
    System.exit(exitCode);
  }

  /**
   * Default action for the root command: print help/usage.
   */
  @Override
  public void run() {
    CommandLine.usage(this, System.err);
  }

  @Command(name = "global_linear", mixinStandardHelpOptions = true,
      description = "Compute min-cost global alignment with linear gap penalty.")
  static final class GlobalLinearCommand extends BaseCommand implements Runnable {
    @Option(names = "--traceback", description = "Include aligned strings in output.")
    boolean traceback;

    /** Executes the {@code global_linear} subcommand pipeline. */
    @Override
    public void run() {
      Inputs inputs = resolveInputs();
      ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
      LinearGapCost gapCost = new LinearGapCost(gap);
      GlobalAligner<LinearGapCost> aligner;
      if (threads > 1) {
        System.err.println("Using wavefront parallelism with " + threads + " threads");
        aligner = new WavefrontLinearAligner(threads);
      } else {
        aligner = new GlobalLinearAligner();
      }

      if (!traceback) {
        int cost = aligner.computeCost(inputs.seq1, inputs.seq2, matrix, gapCost);
        writeOutput(Integer.toString(cost));
        return;
      }

      AlignmentResult result = aligner.align(inputs.seq1, inputs.seq2, matrix, gapCost);
      StringBuilder outBuilder = new StringBuilder();
      outBuilder.append("cost: ").append(result.getCost()).append(System.lineSeparator());
      outBuilder.append(">seq1").append(System.lineSeparator());
      outBuilder.append(TextWrap.wrap(result.getAligned1(), wrap)).append(System.lineSeparator());
      outBuilder.append(">seq2").append(System.lineSeparator());
      outBuilder.append(TextWrap.wrap(result.getAligned2(), wrap));
      writeOutput(outBuilder.toString());
    }
  }

  @Command(name = "global_count", mixinStandardHelpOptions = true,
      description = "Count optimal min-cost global alignments with linear gap penalty.")
  static final class GlobalCountCommand extends BaseCommand implements Runnable {
    /** Executes the {@code global_count} subcommand pipeline. */
    @Override
    public void run() {
      Inputs inputs = resolveInputs();
      ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
      LinearGapCost gapCost = new LinearGapCost(gap);
      GlobalLinearAligner aligner = new GlobalLinearAligner();
      OptimalAlignmentCounter counter = new OptimalAlignmentCounter();

      int cost = aligner.computeCost(inputs.seq1, inputs.seq2, matrix, gapCost);
      BigInteger count = counter.countOptimalAlignments(inputs.seq1, inputs.seq2, matrix, gapCost);

      String output = "cost: " + cost + System.lineSeparator() + "count: " + count;
      writeOutput(output);
    }
  }

  @Command(name = "global_affine", mixinStandardHelpOptions = true,
      description = "Compute min-cost global alignment with affine gap penalty.")
  static final class GlobalAffineCommand extends AffineBaseCommand implements Runnable {
    @Option(names = "--traceback", description = "Include aligned strings in output.")
    boolean traceback;

    /** Executes the {@code global_affine} subcommand pipeline. */
    @Override
    public void run() {
      Inputs inputs = resolveInputs();
      ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
      AffineGapCost gapCost = new AffineGapCost(alpha, beta);
      GlobalAligner<AffineGapCost> aligner;
      if (threads > 1) {
        System.err.println("Using wavefront parallelism with " + threads + " threads");
        aligner = new WavefrontAffineAligner(threads);
      } else {
        aligner = new GlobalAffineAligner();
      }
      OptimalAffineAlignmentCounter counter = new OptimalAffineAlignmentCounter();

      if (!traceback) {
        int cost = aligner.computeCost(inputs.seq1, inputs.seq2, matrix, gapCost);
        BigInteger count = counter.countOptimalAlignments(inputs.seq1, inputs.seq2, matrix, gapCost);
        writeOutput("cost: " + cost + System.lineSeparator() + "count: " + count);
        return;
      }

      AlignmentResult result = aligner.align(inputs.seq1, inputs.seq2, matrix, gapCost);
      BigInteger count = counter.countOptimalAlignments(inputs.seq1, inputs.seq2, matrix, gapCost);
      StringBuilder outBuilder = new StringBuilder();
      outBuilder.append("cost: ").append(result.getCost()).append(System.lineSeparator());
      outBuilder.append("count: ").append(count).append(System.lineSeparator());
      outBuilder.append(">seq1").append(System.lineSeparator());
      outBuilder.append(TextWrap.wrap(result.getAligned1(), wrap)).append(System.lineSeparator());
      outBuilder.append(">seq2").append(System.lineSeparator());
      outBuilder.append(TextWrap.wrap(result.getAligned2(), wrap));
      writeOutput(outBuilder.toString());
    }
  }

  abstract static class BaseCommand {
    @Spec
    CommandSpec spec;

    @Option(names = "--seq1", description = "First input sequence.")
    String seq1Raw;

    @Option(names = "--seq2", description = "Second input sequence.")
    String seq2Raw;

    @Option(names = "--fasta1", description = "Path to FASTA file for sequence 1.")
    Path fasta1;

    @Option(names = "--fasta2", description = "Path to FASTA file for sequence 2.")
    Path fasta2;

    @Option(names = "--matrix", required = true, description = "Path to score matrix file.")
    Path matrixPath;

    @Option(names = "--gap", required = true, description = "Linear gap penalty (non-negative integer).")
    int gap;

    @Option(names = "--wrap", defaultValue = "60", description = "Wrap width for alignment output.")
    int wrap;

    @Option(names = "--out", description = "Write output to file instead of stdout.")
    Path outPath;

    @Option(names = "--threads", description = "Worker threads for wavefront DP (positive integer).")
    int threads = 1;

    /**
     * Resolves sequence inputs from either direct arguments or FASTA files.
     *
     * <p>Exactly one mode must be used:
     * <ul>
     *   <li>Direct mode: {@code --seq1} and {@code --seq2}</li>
     *   <li>FASTA mode: {@code --fasta1} and {@code --fasta2}</li>
     * </ul>
     *
     * @return validated input sequence pair
     * @throws CommandLine.ParameterException if argument combinations are invalid
     */
    Inputs resolveInputs() {
      // Global numeric option validation shared by both subcommands.
      if (gap < 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--gap must be non-negative");
      }
      if (threads <= 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--threads must be positive");
      }
      if (wrap <= 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--wrap must be positive");
      }

      // Enforce exactly one input mode to keep command behavior unambiguous.
      boolean directProvided = seq1Raw != null || seq2Raw != null;
      boolean fastaProvided = fasta1 != null || fasta2 != null;
      if (directProvided == fastaProvided) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
            "Provide exactly one input mode: (--seq1 and --seq2) OR (--fasta1 and --fasta2)");
      }

      Sequence seq1;
      Sequence seq2;
      if (directProvided) {
        // Direct mode requires both sequence values.
        if (seq1Raw == null || seq2Raw == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "Direct mode requires both --seq1 and --seq2");
        }
        seq1 = Sequence.of(seq1Raw);
        seq2 = Sequence.of(seq2Raw);
      } else {
        // FASTA mode requires both input files and reads the first record from each.
        if (fasta1 == null || fasta2 == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "FASTA mode requires both --fasta1 and --fasta2");
        }
        seq1 = FastaReader.readFirst(fasta1);
        seq2 = FastaReader.readFirst(fasta2);
      }
      return new Inputs(seq1, seq2);
    }

    /**
     * Writes command output either to stdout or to the requested output file.
     *
     * @param output text to write
     * @throws CommandLine.ExecutionException if writing to {@code --out} fails
     */
    void writeOutput(String output) {
      if (outPath == null) {
        System.out.println(output);
      } else {
        try {
          // Ensure the output directory exists before writing.
          if (outPath.getParent() != null) {
            Files.createDirectories(outPath.getParent());
          }
          Files.writeString(outPath, output + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw new CommandLine.ExecutionException(spec.commandLine(), "Failed to write --out file", e);
        }
      }
    }
  }

  abstract static class AffineBaseCommand {
    @Spec
    CommandSpec spec;

    @Option(names = "--seq1", description = "First input sequence.")
    String seq1Raw;

    @Option(names = "--seq2", description = "Second input sequence.")
    String seq2Raw;

    @Option(names = "--fasta1", description = "Path to FASTA file for sequence 1.")
    Path fasta1;

    @Option(names = "--fasta2", description = "Path to FASTA file for sequence 2.")
    Path fasta2;

    @Option(names = "--matrix", required = true, description = "Path to score matrix file.")
    Path matrixPath;

    @Option(names = "--alpha", required = true, description = "Affine gap opening penalty (non-negative integer).")
    int alpha;

    @Option(names = "--beta", required = true, description = "Affine gap extension penalty (non-negative integer).")
    int beta;

    @Option(names = "--wrap", defaultValue = "60", description = "Wrap width for alignment output.")
    int wrap;

    @Option(names = "--out", description = "Write output to file instead of stdout.")
    Path outPath;

    @Option(names = "--threads", description = "Worker threads for wavefront DP (positive integer).")
    int threads = 1;

    /**
     * Resolves sequence inputs from either direct arguments or FASTA files for affine commands.
     *
     * <p>Exactly one mode must be used:
     * <ul>
     *   <li>Direct mode: {@code --seq1} and {@code --seq2}</li>
     *   <li>FASTA mode: {@code --fasta1} and {@code --fasta2}</li>
     * </ul>
     *
     * @return validated input sequence pair
     * @throws CommandLine.ParameterException if argument combinations are invalid
     */
    Inputs resolveInputs() {
      if (alpha < 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--alpha must be non-negative");
      }
      if (beta < 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--beta must be non-negative");
      }
      if (threads <= 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--threads must be positive");
      }
      if (wrap <= 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--wrap must be positive");
      }

      boolean directProvided = seq1Raw != null || seq2Raw != null;
      boolean fastaProvided = fasta1 != null || fasta2 != null;
      if (directProvided == fastaProvided) {
        throw new CommandLine.ParameterException(
            spec.commandLine(),
            "Provide exactly one input mode: (--seq1 and --seq2) OR (--fasta1 and --fasta2)");
      }

      Sequence seq1;
      Sequence seq2;
      if (directProvided) {
        if (seq1Raw == null || seq2Raw == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "Direct mode requires both --seq1 and --seq2");
        }
        seq1 = Sequence.of(seq1Raw);
        seq2 = Sequence.of(seq2Raw);
      } else {
        if (fasta1 == null || fasta2 == null) {
          throw new CommandLine.ParameterException(
              spec.commandLine(),
              "FASTA mode requires both --fasta1 and --fasta2");
        }
        seq1 = FastaReader.readFirst(fasta1);
        seq2 = FastaReader.readFirst(fasta2);
      }
      return new Inputs(seq1, seq2);
    }

    /**
     * Writes command output either to stdout or to the requested output file.
     *
     * @param output text to write
     * @throws CommandLine.ExecutionException if writing to {@code --out} fails
     */
    void writeOutput(String output) {
      if (outPath == null) {
        System.out.println(output);
      } else {
        try {
          if (outPath.getParent() != null) {
            Files.createDirectories(outPath.getParent());
          }
          Files.writeString(outPath, output + System.lineSeparator(), StandardCharsets.UTF_8);
        } catch (IOException e) {
          throw new CommandLine.ExecutionException(spec.commandLine(), "Failed to write --out file", e);
        }
      }
    }
  }

  static final class Inputs {
    final Sequence seq1;
    final Sequence seq2;

    /** Holds two validated input sequences for a command execution. */
    Inputs(Sequence seq1, Sequence seq2) {
      this.seq1 = seq1;
      this.seq2 = seq2;
    }
  }

}
