package bioseq.core.io;

import bioseq.core.model.Sequence;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;

/**
 * FASTA reader utilities.
 */
public final class FastaReader {
  private FastaReader() {
  }

  public static Sequence readFirst(Path path) {
    Objects.requireNonNull(path, "path must not be null");
    try (InputStream in = Files.newInputStream(path)) {
      return readFirst(in);
    } catch (NoSuchFileException e) {
      throw new IllegalArgumentException("FASTA file not found: " + path, e);
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read FASTA file: " + path, e);
    }
  }

  public static Sequence readFirst(InputStream in) {
    Objects.requireNonNull(in, "input stream must not be null");

    String id = null;
    boolean hasHeader = false;
    boolean sawNonEmptyLine = false;
    StringBuilder residues = new StringBuilder();

    try (BufferedReader reader =
             new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          continue;
        }

        sawNonEmptyLine = true;

        if (trimmed.charAt(0) == '>') {
          if (hasHeader) {
            break;
          }

          hasHeader = true;
          String header = trimmed.substring(1).trim();
          id = header.isEmpty() ? "seq" : header;
          continue;
        }

        if (id == null) {
          id = "seq";
        }
        residues.append(trimmed);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read FASTA input", e);
    }

    if (!sawNonEmptyLine) {
      throw new IllegalArgumentException("FASTA input is empty");
    }

    if (id == null) {
      id = "seq";
    }

    return new Sequence(id, residues.toString().toUpperCase(Locale.ROOT));
  }
}
