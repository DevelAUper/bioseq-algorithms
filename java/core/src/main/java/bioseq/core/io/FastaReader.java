package bioseq.core.io;

import bioseq.core.model.Sequence;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;

/**
 * FASTA reader utilities. Parsing logic will be implemented later.
 */
public final class FastaReader {
  private FastaReader() {
  }

  public static List<Sequence> parse(Path path) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public static List<Sequence> parse(Reader reader) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
