package bioseq.pairwise.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WavefrontLinearAlignerTest {
  @TempDir
  Path tempDir;

  @Test
  void matchesSequentialImplementationWithOneThread() throws IOException {
    assertEquivalentToSequential(1);
  }

  @Test
  void matchesSequentialImplementationWithTwoThreads() throws IOException {
    assertEquivalentToSequential(2);
  }

  @Test
  void matchesSequentialImplementationWithFourThreads() throws IOException {
    assertEquivalentToSequential(4);
  }

  private void assertEquivalentToSequential(int threadCount) throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix(threadCount));
    GlobalLinearAligner sequential = new GlobalLinearAligner();
    WavefrontLinearAligner wavefront = new WavefrontLinearAligner(threadCount);

    LinearGapCost gap2 = new LinearGapCost(2);
    assertEquals(
        sequential.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap2),
        wavefront.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap2));
    assertEquals(
        sequential.computeCost(Sequence.of("A"), Sequence.of("C"), matrix, gap2),
        wavefront.computeCost(Sequence.of("A"), Sequence.of("C"), matrix, gap2));
    assertEquals(
        sequential.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap2),
        wavefront.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap2));
    assertEquals(
        sequential.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap2),
        wavefront.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap2));

    // Medium deterministic case: identical 100-length strings have known optimal cost 0.
    String medium = "AC".repeat(50);
    int sequentialCost = sequential.computeCost(Sequence.of(medium), Sequence.of(medium), matrix, gap2);
    int wavefrontCost = wavefront.computeCost(Sequence.of(medium), Sequence.of(medium), matrix, gap2);
    assertEquals(0, sequentialCost);
    assertEquals(sequentialCost, wavefrontCost);

    // Traceback result should also match the reference implementation.
    AlignmentResult expected = sequential.align(Sequence.of("AC"), Sequence.of("A"), matrix, gap2);
    AlignmentResult actual = wavefront.align(Sequence.of("AC"), Sequence.of("A"), matrix, gap2);
    assertEquals(expected.getCost(), actual.getCost());
    assertEquals(expected.getAligned1(), actual.getAligned1());
    assertEquals(expected.getAligned2(), actual.getAligned2());
  }

  private Path writeTwoByTwoMatrix(int threadCount) throws IOException {
    Path matrixFile = tempDir.resolve("wavefront-linear-matrix-" + threadCount + ".txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    return matrixFile;
  }
}
