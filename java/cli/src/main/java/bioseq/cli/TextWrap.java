package bioseq.cli;

/**
 * Text wrapping utility for fixed-width CLI output.
 *
 * <p>The algorithm slices the input into contiguous chunks of at most {@code width} characters
 * and joins chunks using platform line separators. No whitespace-aware reflow is performed.
 */
public final class TextWrap {
  /** Utility class; not instantiable. */
  private TextWrap() {
  }

  /**
   * Wraps a string into fixed-width lines.
   *
   * @param text text to wrap
   * @param width maximum number of characters per line
   * @return wrapped text, or an empty string when input is {@code null} or empty
   * @throws IllegalArgumentException if {@code width <= 0}
   */
  public static String wrap(String text, int width) {
    if (width <= 0) {
      throw new IllegalArgumentException("width must be positive");
    }
    if (text == null || text.isEmpty()) {
      return "";
    }

    StringBuilder out = new StringBuilder(text.length() + (text.length() / width) + 1);
    for (int i = 0; i < text.length(); i += width) {
      // Emit the next fixed-width chunk [i, end).
      int end = Math.min(i + width, text.length());
      out.append(text, i, end);
      // Add a separator only when more chunks remain.
      if (end < text.length()) {
        out.append(System.lineSeparator());
      }
    }
    return out.toString();
  }
}
