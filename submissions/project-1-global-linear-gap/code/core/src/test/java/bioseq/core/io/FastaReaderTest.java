package bioseq.core.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.model.Sequence;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FastaReaderTest {
  @Test
  void readFirstParsesMultilineLowercase() {
    String fasta = """
        >seq1
        acg
        tta

        >seq2
        ggg
        """;

    Sequence sequence = FastaReader.readFirst(
        new ByteArrayInputStream(fasta.getBytes(StandardCharsets.UTF_8)));

    assertEquals("seq1", sequence.getId());
    assertEquals("ACGTTA", sequence.getResidues());
  }
}
