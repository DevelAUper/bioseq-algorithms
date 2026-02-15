package bioseq.pairwise.global;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bioseq.core.gap.AffineGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlobalAffineAlignerTest {
  @TempDir
  Path tempDir;

  @Test
  void computeCostMatchesExpectedAffineBehavior() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix());
    GlobalAffineAligner aligner = new GlobalAffineAligner();
    AffineGapCost gap = new AffineGapCost(10, 3);

    // Exact match.
    assertEquals(0, aligner.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap));
    // Single-gap length 1: alpha + beta.
    assertEquals(13, aligner.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap));
    // One contiguous gap of length 2: alpha + 2*beta (not two separate opens).
    assertEquals(16, aligner.computeCost(Sequence.of("AA"), Sequence.of(""), matrix, gap));
    // With one missing symbol, gap length is 1, so cost is alpha + beta.
    assertEquals(13, aligner.computeCost(Sequence.of("AA"), Sequence.of("A"), matrix, gap));

    // Affine extension should keep a single long gap cheaper than two independent openings.
    int longGapCost = aligner.computeCost(Sequence.of("AAA"), Sequence.of("A"), matrix, gap);
    assertEquals(16, longGapCost);
    assertTrue(longGapCost < 26);

    // Simple mismatch-length case still prefers a single affine gap after matching A-A.
    assertEquals(13, aligner.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap));
  }

  @Test
  void alignReturnsExpectedAlignedStringsForSimpleCase() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix());
    GlobalAffineAligner aligner = new GlobalAffineAligner();
    AffineGapCost gap = new AffineGapCost(10, 3);

    var result = aligner.align(Sequence.of("AC"), Sequence.of("A"), matrix, gap);
    assertEquals(13, result.getCost());
    assertEquals("AC", result.getAligned1());
    assertEquals("A-", result.getAligned2());
  }

  @Test
  void countOptimalAlignmentsIsOneForUniqueBestAlignment() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix());
    OptimalAffineAlignmentCounter counter = new OptimalAffineAlignmentCounter();
    AffineGapCost gap = new AffineGapCost(10, 3);

    BigInteger count = counter.countOptimalAlignments(Sequence.of("A"), Sequence.of("A"), matrix, gap);
    assertEquals(BigInteger.ONE, count);
  }

  private Path writeTwoByTwoMatrix() throws IOException {
    Path matrixFile = tempDir.resolve("affine-matrix.txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    return matrixFile;
  }
}
