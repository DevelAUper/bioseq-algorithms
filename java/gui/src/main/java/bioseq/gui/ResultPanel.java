package bioseq.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.math.BigInteger;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Displays alignment outputs and summary metrics.
 *
 * <p>The panel presents total cost, number of optimal alignments, elapsed time, and a FASTA-like
 * formatted alignment preview in a monospaced text area.
 */
public final class ResultPanel extends JPanel {
  private static final int WRAP_WIDTH = 60;

  private final JLabel costLabel;
  private final JLabel countLabel;
  private final JLabel timeLabel;
  private final JTextArea outputArea;

  /**
   * Builds the result panel UI components.
   */
  public ResultPanel() {
    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createTitledBorder("Results"));
    setPreferredSize(new Dimension(900, 260));

    costLabel = new JLabel("Cost: -");
    countLabel = new JLabel("Count: -");
    timeLabel = new JLabel("Time: -");

    JPanel metricsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 2));
    metricsPanel.add(costLabel);
    metricsPanel.add(countLabel);
    metricsPanel.add(timeLabel);

    outputArea = new JTextArea(9, 80);
    outputArea.setEditable(false);
    outputArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));

    JScrollPane outputScroll = new JScrollPane(outputArea);
    add(metricsPanel, BorderLayout.NORTH);
    add(outputScroll, BorderLayout.CENTER);
  }

  /**
   * Renders a successful alignment result.
   *
   * @param cost optimal alignment cost
   * @param count number of optimal alignments
   * @param aligned1 first aligned sequence (with gaps)
   * @param aligned2 second aligned sequence (with gaps)
   * @param timeMs elapsed runtime in milliseconds
   */
  public void showResult(int cost, BigInteger count, String aligned1, String aligned2, long timeMs) {
    costLabel.setText("Cost: " + cost);
    countLabel.setText("Count: " + count);
    timeLabel.setText("Time: " + timeMs + " ms");

    StringBuilder text = new StringBuilder();
    text.append(">seq1").append(System.lineSeparator());
    text.append(wrap(aligned1, WRAP_WIDTH)).append(System.lineSeparator());
    text.append(">seq2").append(System.lineSeparator());
    text.append(wrap(aligned2, WRAP_WIDTH));

    outputArea.setText(text.toString());
    outputArea.setCaretPosition(0);
  }

  /**
   * Displays an error message and clears metric labels.
   *
   * @param message human-readable error details
   */
  public void showError(String message) {
    costLabel.setText("Cost: -");
    countLabel.setText("Count: -");
    timeLabel.setText("Time: -");
    outputArea.setText("Error: " + message);
    outputArea.setCaretPosition(0);
  }

  /**
   * Clears all output and resets summary labels.
   */
  public void clear() {
    costLabel.setText("Cost: -");
    countLabel.setText("Count: -");
    timeLabel.setText("Time: -");
    outputArea.setText("");
  }

  /** Wraps sequence text into fixed-width lines for readability. */
  private static String wrap(String value, int width) {
    if (value == null || value.isEmpty()) {
      return "";
    }
    StringBuilder wrapped = new StringBuilder();
    for (int i = 0; i < value.length(); i += width) {
      int end = Math.min(i + width, value.length());
      wrapped.append(value, i, end);
      if (end < value.length()) {
        wrapped.append(System.lineSeparator());
      }
    }
    return wrapped.toString();
  }
}
