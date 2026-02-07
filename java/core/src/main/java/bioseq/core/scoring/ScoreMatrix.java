package bioseq.core.scoring;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Distance-based scoring matrix for residue pairs.
 */
public final class ScoreMatrix {
  public int score(char a, char b) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  public static ScoreMatrix fromFile(Path path, Alphabet alphabet) throws IOException {
    throw new UnsupportedOperationException("Not implemented yet");
  }
}
