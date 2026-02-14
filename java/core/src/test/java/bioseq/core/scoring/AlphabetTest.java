package bioseq.core.scoring;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AlphabetTest {
  @Test
  void validateReportsInvalidResidueAndPosition() {
    Alphabet alphabet = Alphabet.fromSymbols(List.of('A', 'C', 'G', 'T'));

    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class,
        () -> alphabet.validate("ACxT"));

    assertTrue(error.getMessage().contains("'X'"));
    assertTrue(error.getMessage().contains("position 3"));
  }
}
