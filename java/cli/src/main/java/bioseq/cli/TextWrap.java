package bioseq.cli;

/**
 * Text wrapping utility for CLI sequence output.
 */
public final class TextWrap {
  private TextWrap() {
  }

  public static String wrap(String text, int width) {
    if (width <= 0) {
      throw new IllegalArgumentException("width must be positive");
    }
    if (text == null || text.isEmpty()) {
      return "";
    }

    StringBuilder out = new StringBuilder(text.length() + (text.length() / width) + 1);
    for (int i = 0; i < text.length(); i += width) {
      int end = Math.min(i + width, text.length());
      out.append(text, i, end);
      if (end < text.length()) {
        out.append(System.lineSeparator());
      }
    }
    return out.toString();
  }
}
