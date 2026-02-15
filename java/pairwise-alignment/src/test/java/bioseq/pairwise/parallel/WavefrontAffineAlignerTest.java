package bioseq.pairwise.parallel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalAffineAligner;
import bioseq.pairwise.model.AlignmentResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WavefrontAffineAlignerTest {
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
    GlobalAffineAligner sequential = new GlobalAffineAligner();
    try (WavefrontAffineAligner wavefront = new WavefrontAffineAligner(threadCount)) {
      AffineGapCost gap = new AffineGapCost(10, 3);

      assertEquals(
          sequential.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap),
          wavefront.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap));
      assertEquals(
          sequential.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap),
          wavefront.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap));
      assertEquals(
          sequential.computeCost(Sequence.of("AA"), Sequence.of(""), matrix, gap),
          wavefront.computeCost(Sequence.of("AA"), Sequence.of(""), matrix, gap));
      assertEquals(
          sequential.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap),
          wavefront.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap));

      // Medium deterministic case: identical 100-length strings have known optimal cost 0.
      String medium = "AC".repeat(50);
      int sequentialCost = sequential.computeCost(Sequence.of(medium), Sequence.of(medium), matrix, gap);
      int wavefrontCost = wavefront.computeCost(Sequence.of(medium), Sequence.of(medium), matrix, gap);
      assertEquals(0, sequentialCost);
      assertEquals(sequentialCost, wavefrontCost);

      AlignmentResult expected = sequential.align(Sequence.of("AC"), Sequence.of("A"), matrix, gap);
      AlignmentResult actual = wavefront.align(Sequence.of("AC"), Sequence.of("A"), matrix, gap);
      assertEquals(expected.getCost(), actual.getCost());
      assertEquals(expected.getAligned1(), actual.getAligned1());
      assertEquals(expected.getAligned2(), actual.getAligned2());
    }
  }

  private Path writeTwoByTwoMatrix(int threadCount) throws IOException {
    Path matrixFile = tempDir.resolve("wavefront-affine-matrix-" + threadCount + ".txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    return matrixFile;
  }
}
