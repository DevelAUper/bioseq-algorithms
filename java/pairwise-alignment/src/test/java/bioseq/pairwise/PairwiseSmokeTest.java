package bioseq.pairwise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.global.OptimalAlignmentCounter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PairwiseSmokeTest {
  @TempDir
  Path tempDir;

  @Test
  void globalLinearComputeCostMatchesExpectedValue() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix());
    GlobalLinearAligner aligner = new GlobalLinearAligner();

    int cost = aligner.computeCost(Sequence.of("AC"), Sequence.of("AC"), matrix, new LinearGapCost(2));
    assertEquals(0, cost);
  }

  @Test
  void optimalAlignmentCounterMatchesExpectedValue() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeTwoByTwoMatrix());
    OptimalAlignmentCounter counter = new OptimalAlignmentCounter();

    BigInteger count = counter.countOptimalAlignments(
        Sequence.of("AA"),
        Sequence.of("A"),
        matrix,
        new LinearGapCost(1));
    assertEquals(BigInteger.valueOf(2), count);
  }

  private Path writeTwoByTwoMatrix() throws IOException {
    Path matrixFile = tempDir.resolve("matrix.txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    return matrixFile;
  }
}
