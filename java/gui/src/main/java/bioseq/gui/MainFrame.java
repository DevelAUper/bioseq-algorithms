package bioseq.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Main application window for interactive sequence alignment.
 *
 * <p>The frame coordinates three UI areas:
 * <ul>
 *   <li>Top: algorithm selection and execution controls</li>
 *   <li>Center: sequence and scoring input configuration</li>
 *   <li>Bottom: computed cost, count, timing, and alignment output</li>
 * </ul>
 */
public final class MainFrame extends JFrame {
  private static final String ALGORITHM_LINEAR = "Global Linear";
  private static final String ALGORITHM_AFFINE = "Global Affine";

  private final JComboBox<String> algorithmSelector;
  private final JButton runButton;
  private final InputPanel inputPanel;
  private final ResultPanel resultPanel;

  /**
   * Builds and wires the main GUI layout.
   */
  public MainFrame() {
    super("BioSeq Algorithms — Sequence Alignment Toolkit");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(900, 650);
    setLocationRelativeTo(null);
    setLayout(new BorderLayout(10, 10));

    algorithmSelector = new JComboBox<>(new String[] {ALGORITHM_LINEAR, ALGORITHM_AFFINE});
    runButton = new JButton("Run");
    inputPanel = new InputPanel();
    resultPanel = new ResultPanel();

    add(buildTopPanel(), BorderLayout.NORTH);
    add(inputPanel, BorderLayout.CENTER);
    add(resultPanel, BorderLayout.SOUTH);

    inputPanel.setAlgorithm(ALGORITHM_LINEAR);
    algorithmSelector.addActionListener(e -> inputPanel.setAlgorithm(getSelectedAlgorithmLabel()));
    runButton.addActionListener(e -> runAlignment());
  }

  /** Creates the top control strip with algorithm selector and run trigger. */
  private JPanel buildTopPanel() {
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    topPanel.setBorder(BorderFactory.createTitledBorder("Alignment Task"));
    topPanel.add(new JLabel("Algorithm:"));
    topPanel.add(algorithmSelector);
    topPanel.add(runButton);
    return topPanel;
  }

  /** Validates input, starts a background alignment run, and updates button state. */
  private void runAlignment() {
    try {
      String sequence1 = inputPanel.getSequence1();
      String sequence2 = inputPanel.getSequence2();
      Path matrixPath = inputPanel.getMatrixPath();

      if (sequence1.isEmpty()) {
        throw new IllegalArgumentException("Sequence 1 is required.");
      }
      if (sequence2.isEmpty()) {
        throw new IllegalArgumentException("Sequence 2 is required.");
      }
      if (matrixPath == null) {
        throw new IllegalArgumentException("Please select a score matrix file.");
      }

      AlignmentRunner worker = new AlignmentRunner(
          selectedAlgorithm(),
          sequence1,
          sequence2,
          matrixPath,
          inputPanel.getGapPenalty(),
          inputPanel.getAlpha(),
          inputPanel.getBeta(),
          inputPanel.getThreadCount(),
          resultPanel,
          () -> runButton.setEnabled(true));

      runButton.setEnabled(false);
      resultPanel.clear();
      worker.execute();
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /** Returns the selected algorithm enum used by the background runner. */
  private AlignmentRunner.Algorithm selectedAlgorithm() {
    return ALGORITHM_AFFINE.equals(getSelectedAlgorithmLabel())
        ? AlignmentRunner.Algorithm.GLOBAL_AFFINE
        : AlignmentRunner.Algorithm.GLOBAL_LINEAR;
  }

  /** Returns the current algorithm label from the combo box. */
  private String getSelectedAlgorithmLabel() {
    Object selected = algorithmSelector.getSelectedItem();
    return selected == null ? ALGORITHM_LINEAR : selected.toString();
  }
}
