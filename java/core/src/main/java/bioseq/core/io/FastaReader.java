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
 * Utility methods for reading sequences from FASTA input.
 *
 * <p>The reader focuses on the first record only, which matches the CLI contract where each input
 * source contributes exactly one sequence for pairwise alignment.
 */
public final class FastaReader {
  /** Utility class; not instantiable. */
  private FastaReader() {
  }

  /**
   * Reads the first sequence record from a FASTA file path.
   *
   * @param path path to a UTF-8 FASTA file
   * @return first parsed sequence, with residues normalized to uppercase
   * @throws NullPointerException if {@code path} is {@code null}
   * @throws IllegalArgumentException if the file is missing or the input is empty
   * @throws UncheckedIOException if an I/O error occurs while reading
   */
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

  /**
   * Reads the first sequence record from a FASTA stream.
   *
   * <p>Parsing behavior:
   * <ul>
   *   <li>Lines starting with {@code '>'} are treated as headers.</li>
   *   <li>Sequence lines may span multiple lines and are concatenated.</li>
   *   <li>Reading stops when a second header is encountered.</li>
   *   <li>If no header is present, the default identifier {@code seq} is used.</li>
   * </ul>
   *
   * @param in FASTA input stream (consumed and closed by this method)
   * @return first parsed sequence, with residues normalized to uppercase
   * @throws NullPointerException if {@code in} is {@code null}
   * @throws IllegalArgumentException if the input contains no non-empty lines
   * @throws UncheckedIOException if an I/O error occurs while reading
   */
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
        // Ignore blank lines between FASTA content.
        if (trimmed.isEmpty()) {
          continue;
        }

        sawNonEmptyLine = true;

        // Header line: start of a record. If we already saw one header, the first record is complete.
        if (trimmed.charAt(0) == '>') {
          if (hasHeader) {
            break;
          }

          hasHeader = true;
          String header = trimmed.substring(1).trim();
          // Empty headers are normalized to the default identifier.
          id = header.isEmpty() ? "seq" : header;
          continue;
        }

        // Headerless FASTA-like input defaults to identifier "seq".
        if (id == null) {
          id = "seq";
        }
        // Sequence residues may span multiple lines; concatenate in read order.
        residues.append(trimmed);
      }
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to read FASTA input", e);
    }

    // Reject fully empty inputs (including whitespace-only files).
    if (!sawNonEmptyLine) {
      throw new IllegalArgumentException("FASTA input is empty");
    }

    // If input started with residues before any header, keep the default identifier.
    if (id == null) {
      id = "seq";
    }

    // Store normalized uppercase residues so downstream matrix lookups are consistent.
    return new Sequence(id, residues.toString().toUpperCase(Locale.ROOT));
  }
}
