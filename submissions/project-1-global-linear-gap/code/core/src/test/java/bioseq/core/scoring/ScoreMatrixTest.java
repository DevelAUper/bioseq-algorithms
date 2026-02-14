package bioseq.core.scoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ScoreMatrixTest {
  @TempDir
  Path tempDir;

  @Test
  void parsesPhylipLikeMatrix() throws IOException {
    Path matrixFile = tempDir.resolve("matrix.txt");
    Files.writeString(matrixFile, """
        3
        A 0 2 3
        C 2 0 1
        G 3 1 0
        """);

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixFile);

    assertTrue(matrix.alphabet().contains('A'));
    assertTrue(matrix.alphabet().contains('c'));
    assertEquals(0, matrix.cost('A', 'A'));
    assertEquals(1, matrix.cost('C', 'G'));
    assertEquals(3, matrix.cost('g', 'A'));
  }
}
