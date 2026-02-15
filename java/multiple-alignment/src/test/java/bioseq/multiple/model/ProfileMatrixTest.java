package bioseq.multiple.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import bioseq.core.scoring.Alphabet;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProfileMatrixTest {
  @Test
  void computesFrequenciesFromAlignedSequences() {
    ProfileMatrix profile = new ProfileMatrix(
        List.of("A-", "AC"),
        Alphabet.fromSymbols(List.of('A', 'C')));

    assertEquals(2, profile.length());
    assertEquals(2, profile.sequenceCount());
    assertEquals(1.0, profile.frequency(0, 'A'), 1e-9);
    assertEquals(0.5, profile.frequency(1, 'C'), 1e-9);
    assertEquals(0.0, profile.frequency(1, 'A'), 1e-9);
  }
}
