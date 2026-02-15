package bioseq.gui;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalAffineAligner;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.global.OptimalAffineAlignmentCounter;
import bioseq.pairwise.global.OptimalAlignmentCounter;
import bioseq.pairwise.model.AlignmentResult;
import bioseq.pairwise.parallel.WavefrontAffineAligner;
import bioseq.pairwise.parallel.WavefrontLinearAligner;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javax.swing.SwingWorker;

/**
 * Background worker that bridges GUI inputs to pairwise alignment implementations.
 *
 * <p>This worker performs matrix parsing, sequence creation, alignment, and optimal-count
 * computation off the Event Dispatch Thread, then updates the {@link ResultPanel} on completion.
 */
public final class AlignmentRunner extends SwingWorker<AlignmentRunner.RunOutput, Void> {
  /** Supported GUI algorithm selections. */
  public enum Algorithm {
    GLOBAL_LINEAR,
    GLOBAL_AFFINE
  }

  private final Algorithm algorithm;
  private final String sequence1;
  private final String sequence2;
  private final Path matrixPath;
  private final int gapPenalty;
  private final int alpha;
  private final int beta;
  private final int threadCount;
  private final ResultPanel resultPanel;
  private final Runnable onComplete;

  /**
   * Creates a background alignment job.
   *
   * @param algorithm algorithm variant to execute
   * @param sequence1 first sequence residues
   * @param sequence2 second sequence residues
   * @param matrixPath path to score matrix file
   * @param gapPenalty linear gap penalty
   * @param alpha affine gap opening penalty
   * @param beta affine gap extension penalty
   * @param threadCount requested worker threads
   * @param resultPanel result target for completion updates
   * @param onComplete callback invoked on EDT when work finishes
   */
  public AlignmentRunner(
      Algorithm algorithm,
      String sequence1,
      String sequence2,
      Path matrixPath,
      int gapPenalty,
      int alpha,
      int beta,
      int threadCount,
      ResultPanel resultPanel,
      Runnable onComplete) {
    this.algorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
    this.sequence1 = Objects.requireNonNull(sequence1, "sequence1 must not be null");
    this.sequence2 = Objects.requireNonNull(sequence2, "sequence2 must not be null");
    this.matrixPath = Objects.requireNonNull(matrixPath, "matrixPath must not be null");
    this.gapPenalty = gapPenalty;
    this.alpha = alpha;
    this.beta = beta;
    this.threadCount = threadCount;
    this.resultPanel = Objects.requireNonNull(resultPanel, "resultPanel must not be null");
    this.onComplete = Objects.requireNonNull(onComplete, "onComplete must not be null");
  }

  /** Executes alignment and counting work on a background thread. */
  @Override
  protected RunOutput doInBackground() {
    long startedNanos = System.nanoTime();

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
    Sequence seq1 = Sequence.of(sequence1);
    Sequence seq2 = Sequence.of(sequence2);

    AlignmentResult alignmentResult;
    BigInteger count;

    if (algorithm == Algorithm.GLOBAL_AFFINE) {
      AffineGapCost gap = new AffineGapCost(alpha, beta);
      if (threadCount > 1) {
        try (WavefrontAffineAligner aligner = new WavefrontAffineAligner(threadCount)) {
          alignmentResult = aligner.align(seq1, seq2, matrix, gap);
        }
      } else {
        alignmentResult = new GlobalAffineAligner().align(seq1, seq2, matrix, gap);
      }
      count = new OptimalAffineAlignmentCounter().countOptimalAlignments(seq1, seq2, matrix, gap);
    } else {
      LinearGapCost gap = new LinearGapCost(gapPenalty);
      if (threadCount > 1) {
        try (WavefrontLinearAligner aligner = new WavefrontLinearAligner(threadCount)) {
          alignmentResult = aligner.align(seq1, seq2, matrix, gap);
        }
      } else {
        alignmentResult = new GlobalLinearAligner().align(seq1, seq2, matrix, gap);
      }
      count = new OptimalAlignmentCounter().countOptimalAlignments(seq1, seq2, matrix, gap);
    }

    long elapsedMs = (System.nanoTime() - startedNanos) / 1_000_000L;
    return new RunOutput(
        alignmentResult.getCost(),
        count,
        alignmentResult.getAligned1(),
        alignmentResult.getAligned2(),
        elapsedMs);
  }

  /** Updates the result panel and completion state after background execution. */
  @Override
  protected void done() {
    try {
      RunOutput output = get();
      resultPanel.showResult(
          output.cost,
          output.count,
          output.alignedSequence1,
          output.alignedSequence2,
          output.elapsedMs);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      resultPanel.showError("Alignment was interrupted.");
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      String message = cause == null ? "Unknown error." : cause.getMessage();
      resultPanel.showError(message == null ? "Unknown error." : message);
    } finally {
      onComplete.run();
    }
  }

  /** Immutable worker output container used between background and EDT phases. */
  static final class RunOutput {
    final int cost;
    final BigInteger count;
    final String alignedSequence1;
    final String alignedSequence2;
    final long elapsedMs;

    /** Stores computed alignment metrics and aligned strings. */
    RunOutput(int cost, BigInteger count, String alignedSequence1, String alignedSequence2, long elapsedMs) {
      this.cost = cost;
      this.count = count;
      this.alignedSequence1 = alignedSequence1;
      this.alignedSequence2 = alignedSequence2;
      this.elapsedMs = elapsedMs;
    }
  }
}
