package bioseq.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TextWrapTest {
  @Test
  void wrapsAtFixedWidth() {
    assertEquals("ABCD" + System.lineSeparator() + "EFGH" + System.lineSeparator() + "IJ",
        TextWrap.wrap("ABCDEFGHIJ", 4));
  }
}
