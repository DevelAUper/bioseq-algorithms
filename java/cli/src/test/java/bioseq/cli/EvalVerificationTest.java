package bioseq.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.gap.LinearGapCost;
import bioseq.core.model.Sequence;
import bioseq.core.scoring.ScoreMatrix;
import bioseq.pairwise.global.GlobalLinearAligner;
import bioseq.pairwise.global.OptimalAlignmentCounter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Verifies Project 1 evaluation tables against the CLI's shared algorithm classes.
 *
 * <p>The test reads the full multi-record FASTA manually (all 5 records), because
 * {@code FastaReader.readFirst(...)} intentionally returns only the first record.
 */
class EvalVerificationTest {
  private static final List<String> IDS = List.of("seq1", "seq2", "seq3", "seq4", "seq5");

  /**
   * Verifies all 10 unique pairwise min-cost values (i &lt; j) for gap=5 and project1_M matrix.
   */
  @Test
  void verifyCostTable() throws IOException {
    Path fastaPath = Paths.get("..", "data", "fasta", "project1_eval_seqs.fa");
    Path matrixPath = Paths.get("..", "data", "matrices", "project1_M.txt");

    Assumptions.assumeTrue(Files.exists(fastaPath), "Missing FASTA: " + fastaPath);
    Assumptions.assumeTrue(Files.exists(matrixPath), "Missing matrix: " + matrixPath);

    Map<String, Sequence> sequences = readAllFastaRecordsById(fastaPath);
    Assumptions.assumeTrue(sequences.keySet().containsAll(IDS), "Missing one or more seq1..seq5 records");

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
    GlobalLinearAligner aligner = new GlobalLinearAligner();
    LinearGapCost gap = new LinearGapCost(5);

    assertEquals(226, aligner.computeCost(sequences.get("seq1"), sequences.get("seq2"), matrix, gap));
    assertEquals(206, aligner.computeCost(sequences.get("seq1"), sequences.get("seq3"), matrix, gap));
    assertEquals(202, aligner.computeCost(sequences.get("seq1"), sequences.get("seq4"), matrix, gap));
    assertEquals(209, aligner.computeCost(sequences.get("seq1"), sequences.get("seq5"), matrix, gap));
    assertEquals(239, aligner.computeCost(sequences.get("seq2"), sequences.get("seq3"), matrix, gap));
    assertEquals(223, aligner.computeCost(sequences.get("seq2"), sequences.get("seq4"), matrix, gap));
    assertEquals(220, aligner.computeCost(sequences.get("seq2"), sequences.get("seq5"), matrix, gap));
    assertEquals(219, aligner.computeCost(sequences.get("seq3"), sequences.get("seq4"), matrix, gap));
    assertEquals(205, aligner.computeCost(sequences.get("seq3"), sequences.get("seq5"), matrix, gap));
    assertEquals(210, aligner.computeCost(sequences.get("seq4"), sequences.get("seq5"), matrix, gap));
  }

  /**
   * Verifies all 10 unique pairwise optimal-alignment counts (i &lt; j) for gap=5 and project1_M matrix.
   */
  @Test
  void verifyCountTable() throws IOException {
    Path fastaPath = Paths.get("..", "data", "fasta", "project1_eval_seqs.fa");
    Path matrixPath = Paths.get("..", "data", "matrices", "project1_M.txt");

    Assumptions.assumeTrue(Files.exists(fastaPath), "Missing FASTA: " + fastaPath);
    Assumptions.assumeTrue(Files.exists(matrixPath), "Missing matrix: " + matrixPath);

    Map<String, Sequence> sequences = readAllFastaRecordsById(fastaPath);
    Assumptions.assumeTrue(sequences.keySet().containsAll(IDS), "Missing one or more seq1..seq5 records");

    ScoreMatrix matrix = ScoreMatrix.fromPhylipLikeFile(matrixPath);
    OptimalAlignmentCounter counter = new OptimalAlignmentCounter();
    LinearGapCost gap = new LinearGapCost(5);

    assertEquals(BigInteger.valueOf(90), counter.countOptimalAlignments(sequences.get("seq1"), sequences.get("seq2"), matrix, gap));
    assertEquals(BigInteger.valueOf(192), counter.countOptimalAlignments(sequences.get("seq1"), sequences.get("seq3"), matrix, gap));
    assertEquals(BigInteger.valueOf(3168), counter.countOptimalAlignments(sequences.get("seq1"), sequences.get("seq4"), matrix, gap));
    assertEquals(BigInteger.valueOf(96), counter.countOptimalAlignments(sequences.get("seq1"), sequences.get("seq5"), matrix, gap));
    assertEquals(BigInteger.valueOf(384), counter.countOptimalAlignments(sequences.get("seq2"), sequences.get("seq3"), matrix, gap));
    assertEquals(BigInteger.valueOf(384), counter.countOptimalAlignments(sequences.get("seq2"), sequences.get("seq4"), matrix, gap));
    assertEquals(BigInteger.valueOf(1824), counter.countOptimalAlignments(sequences.get("seq2"), sequences.get("seq5"), matrix, gap));
    assertEquals(BigInteger.valueOf(138240), counter.countOptimalAlignments(sequences.get("seq3"), sequences.get("seq4"), matrix, gap));
    assertEquals(BigInteger.valueOf(972), counter.countOptimalAlignments(sequences.get("seq3"), sequences.get("seq5"), matrix, gap));
    assertEquals(BigInteger.valueOf(16), counter.countOptimalAlignments(sequences.get("seq4"), sequences.get("seq5"), matrix, gap));
  }

  private static Map<String, Sequence> readAllFastaRecordsById(Path fastaPath) throws IOException {
    List<String> lines = Files.readAllLines(fastaPath, StandardCharsets.UTF_8);
    List<Sequence> parsed = new ArrayList<>();

    String currentId = null;
    StringBuilder residues = new StringBuilder();
    for (String raw : lines) {
      String line = raw.trim();
      if (line.isEmpty()) {
        continue;
      }
      if (line.startsWith(">")) {
        if (currentId != null) {
          parsed.add(new Sequence(currentId, residues.toString().toUpperCase(Locale.ROOT)));
        }
        currentId = line.substring(1).trim();
        residues.setLength(0);
      } else if (currentId != null) {
        residues.append(line);
      }
    }

    if (currentId != null) {
      parsed.add(new Sequence(currentId, residues.toString().toUpperCase(Locale.ROOT)));
    }

    Map<String, Sequence> byId = new LinkedHashMap<>();
    for (Sequence sequence : parsed) {
      byId.put(sequence.getId(), sequence);
    }
    return byId;
  }
}
