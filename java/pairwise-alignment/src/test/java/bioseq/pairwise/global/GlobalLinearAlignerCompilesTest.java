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
  void computeCostRunsForTrivialEmptySequences() throws IOException {
    Path matrixFile = tempDir.resolve("tiny-matrix.txt");
    Files.writeString(matrixFile, """
        1
        A 0
        """);

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);
    GlobalLinearAligner aligner = new GlobalLinearAligner();
    LinearGapCost gap = new LinearGapCost(2);

    int cost = aligner.computeCost(Sequence.of(""), Sequence.of(""), matrix, gap);
    assertEquals(0, cost);
  }
}
