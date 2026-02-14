package bioseq.cli;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.io.FastaReader;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.global.OptimalAlignmentCounter;
import bioseq.pairwise.model.AlignmentResult;
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
 */
@Command(
    name = "bioseq-cli",
    mixinStandardHelpOptions = true,
    subcommands = {BioseqCli.GlobalLinearCommand.class, BioseqCli.GlobalCountCommand.class})
public final class BioseqCli implements Runnable {
  private BioseqCli() {
  }

  public static void main(String[] args) {
    int exitCode = new CommandLine(new BioseqCli()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public void run() {
    CommandLine.usage(this, System.err);
  }

  @Command(name = "global_linear", mixinStandardHelpOptions = true,
      description = "Compute min-cost global alignment with linear gap penalty.")
  static final class GlobalLinearCommand extends BaseCommand implements Runnable {
    @Option(names = "--traceback", description = "Include aligned strings in output.")
    boolean traceback;

    @Override
    public void run() {
      Inputs inputs = resolveInputs();
      ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
      LinearGapCost gapCost = new LinearGapCost(gap);
      GlobalLinearAligner aligner = new GlobalLinearAligner();

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

    @Option(names = "--threads", description = "Reserved for future parallelism support.")
    int threads = 1;

    Inputs resolveInputs() {
      if (gap < 0) {
        throw new CommandLine.ParameterException(spec.commandLine(), "--gap must be non-negative");
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

    Inputs(Sequence seq1, Sequence seq2) {
      this.seq1 = seq1;
      this.seq2 = seq2;
    }
  }

}
