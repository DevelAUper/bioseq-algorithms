package bioseq.pairwise.global;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OptimalAlignmentCounterTest {
  @TempDir
  Path tempDir;

  @Test
  void countsTwoOptimalAlignmentsInNonDegenerateCase() throws IOException {
    Path matrixFile = tempDir.resolve("a-only-matrix.txt");
    Files.writeString(matrixFile, """
        1
        A 0
        """);
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);

    OptimalAlignmentCounter counter = new OptimalAlignmentCounter();
    BigInteger count = counter.countOptimalAlignments(
        Sequence.of("AA"),
        Sequence.of("A"),
        matrix,
        new LinearGapCost(1));

    // Two optimal ways: match first A then delete second, or delete first then match second.
    assertEquals(BigInteger.valueOf(2), count);
  }

  @Test
  void countsUniqueOptimalAlignmentWhenSubstitutionIsBest() throws IOException {
    Path matrixFile = tempDir.resolve("ac-matrix.txt");
    Files.writeString(matrixFile, """
        2
        A 0 5
        C 5 0
        """);
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);

    OptimalAlignmentCounter counter = new OptimalAlignmentCounter();
    BigInteger count = counter.countOptimalAlignments(
        Sequence.of("A"),
        Sequence.of("C"),
        matrix,
        new LinearGapCost(3));

    assertEquals(BigInteger.ONE, count);
  }
}
