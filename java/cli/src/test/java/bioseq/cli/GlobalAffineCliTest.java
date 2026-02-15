package bioseq.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

class GlobalAffineCliTest {
  @TempDir
  Path tempDir;

  @Test
  void globalAffineSubcommandPrintsZeroCostForPerfectMatch() throws Exception {
    Path matrixPath = tempDir.resolve("single-a-matrix.txt");
    Files.writeString(matrixPath, """
        1
        A 0
        """);

    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    PrintStream originalOut = System.out;
    try (PrintStream capture = new PrintStream(outBuffer, true, StandardCharsets.UTF_8)) {
      System.setOut(capture);

      Constructor<BioseqCli> constructor = BioseqCli.class.getDeclaredConstructor();
      constructor.setAccessible(true);
      BioseqCli rootCommand = constructor.newInstance();

      int exitCode = new CommandLine(rootCommand).execute(
          "global_affine",
          "--seq1", "A",
          "--seq2", "A",
          "--matrix", matrixPath.toString(),
          "--alpha", "10",
          "--beta", "3");

      assertEquals(0, exitCode);
    } finally {
      System.setOut(originalOut);
    }

    String output = outBuffer.toString(StandardCharsets.UTF_8);
    assertTrue(output.contains("cost: 0"));
  }
}
