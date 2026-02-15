package bioseq.multiple.heuristic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.multiple.model.MultipleAlignmentResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CenterStarAlignerTest {
  @TempDir
  Path tempDir;

  @Test
  void alignReturnsEqualLengthRowsAndPositiveCost() throws IOException {
    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(writeSimpleMatrix());
    LinearGapCost gap = new LinearGapCost(2);
    CenterStarAligner aligner = new CenterStarAligner(matrix, gap);

    MultipleAlignmentResult result = aligner.align(
        List.of(
            new Sequence("s1", "AA"),
            new Sequence("s2", "AC"),
            new Sequence("s3", "CC")),
        matrix,
        gap);

    assertEquals(3, result.getAlignedSequences().size());
    int alignedLength = result.getAlignedSequences().get(0).length();
    assertTrue(alignedLength > 0);
    assertEquals(alignedLength, result.getAlignedSequences().get(1).length());
    assertEquals(alignedLength, result.getAlignedSequences().get(2).length());
    assertTrue(result.getCost() > 0);
  }

  private Path writeSimpleMatrix() throws IOException {
    Path matrixFile = tempDir.resolve("center-star-matrix.txt");
    Files.writeString(matrixFile, """
        2
        A 0 2
        C 2 0
        """);
    return matrixFile;
  }
}
