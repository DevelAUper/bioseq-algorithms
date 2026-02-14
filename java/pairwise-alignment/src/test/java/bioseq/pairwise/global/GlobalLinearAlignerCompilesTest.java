package bioseq.pairwise.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GlobalLinearAlignerCompilesTest {
  @TempDir
  Path tempDir;

  @Test
  void computeCostReturnsExpectedMinCosts() throws IOException {
    Path matrixFile = tempDir.resolve("tiny-matrix.txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);
    GlobalLinearAligner aligner = new GlobalLinearAligner();
    LinearGapCost gap = new LinearGapCost(2);

    // Match is free.
    assertEquals(0, aligner.computeCost(Sequence.of("A"), Sequence.of("A"), matrix, gap));
    // With linear gaps, A vs C can be realized as (A,-) + (-,C): 2 + 2 = 4, which beats mismatch cost 5.
    assertEquals(4, aligner.computeCost(Sequence.of("A"), Sequence.of("C"), matrix, gap));
    // Aligning against empty sequence is one deletion/insertion per residue.
    assertEquals(2, aligner.computeCost(Sequence.of("A"), Sequence.of(""), matrix, gap));
    // Best for AC vs A is matching A-A then deleting C (cost 2), not forcing C-A mismatch.
    assertEquals(2, aligner.computeCost(Sequence.of("AC"), Sequence.of("A"), matrix, gap));
  }

  @Test
  void alignReturnsExpectedTracebackAndCost() throws IOException {
    Path matrixFile = tempDir.resolve("tiny-matrix-align.txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);
    GlobalLinearAligner aligner = new GlobalLinearAligner();

    // AC vs A: optimal is match A-A and delete C.
    var result1 = aligner.align(Sequence.of("AC"), Sequence.of("A"), matrix, new LinearGapCost(2));
    assertEquals(2, result1.getCost());
    assertEquals("AC", result1.getAligned1());
    assertEquals("A-", result1.getAligned2());

    // Here substitution is cheaper than opening two opposite gaps (5 < 3 + 3), so we expect DIAG.
    var result2 = aligner.align(Sequence.of("A"), Sequence.of("C"), matrix, new LinearGapCost(3));
    assertEquals(5, result2.getCost());
    assertEquals("A", result2.getAligned1());
    assertEquals("C", result2.getAligned2());
  }
}
