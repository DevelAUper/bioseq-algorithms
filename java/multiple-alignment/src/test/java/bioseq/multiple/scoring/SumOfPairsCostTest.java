package bioseq.multiple.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.scoring.ScoreMatrix;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SumOfPairsCostTest {
  @TempDir
  Path tempDir;

  @Test
  void columnCostComputesPairwiseSum() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeSingleSymbolMatrix());
    SumOfPairsCost sp = new SumOfPairsCost();

    // Pairs: (A,A)=0, (A,-)=2, (A,-)=2 => total 4.
    assertEquals(4, sp.columnCost(new char[] {'A', 'A', '-'}, matrix, new LinearGapCost(2)));
  }

  @Test
  void totalCostSumsColumnCosts() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeSingleSymbolMatrix());
    SumOfPairsCost sp = new SumOfPairsCost();

    // Alignment columns:
    // col0: A,A,- => 4
    // col1: -,A,A => 4
    // total => 8
    List<String> aligned = List.of("A-", "AA", "-A");
    assertEquals(8, sp.totalCost(aligned, matrix, new LinearGapCost(2)));
  }

  private Path writeSingleSymbolMatrix() throws IOException {
    Path matrixFile = tempDir.resolve("single-a-matrix.txt");
    Files.writeString(matrixFile, """
        1
        A 0
        """);
    return matrixFile;
  }
}
