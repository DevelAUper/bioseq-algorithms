package bioseq.gui;

import bioseq.core.io.FastaReader;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Collects user inputs for pairwise alignment tasks.
 *
 * <p>The panel supports two sequence input modes (direct text or FASTA files), score-matrix
 * selection, gap-parameter controls for linear/affine models, and thread-count configuration.
 * The visible gap controls are switched by the selected algorithm in {@link MainFrame}.
 */
public final class InputPanel extends JPanel {
  private static final String MODE_DIRECT = "DIRECT";
  private static final String MODE_FASTA = "FASTA";
  private static final String GAP_LINEAR = "LINEAR";
  private static final String GAP_AFFINE = "AFFINE";

  private final JRadioButton directInputButton;
  private final JRadioButton fastaFileButton;
  private final JTextArea sequence1Area;
  private final JTextArea sequence2Area;
  private final JLabel fasta1PathLabel;
  private final JLabel fasta2PathLabel;
  private final JLabel matrixPathLabel;
  private final CardLayout modeCards;
  private final CardLayout gapCards;
  private final JPanel modePanel;
  private final JPanel gapPanel;
  private final JSpinner gapSpinner;
  private final JSpinner alphaSpinner;
  private final JSpinner betaSpinner;
  private final JSpinner threadSpinner;

  private Path fasta1Path;
  private Path fasta2Path;
  private Path matrixPath;

  /**
   * Builds the input panel and initializes all controls.
   */
  public InputPanel() {
    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    directInputButton = new JRadioButton("Direct Input", true);
    fastaFileButton = new JRadioButton("FASTA File");

    sequence1Area = buildSequenceArea();
    sequence2Area = buildSequenceArea();
    fasta1PathLabel = new JLabel("No file selected");
    fasta2PathLabel = new JLabel("No file selected");
    matrixPathLabel = new JLabel("No file selected");

    gapSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 100000, 1));
    alphaSpinner = new JSpinner(new SpinnerNumberModel(10, 0, 100000, 1));
    betaSpinner = new JSpinner(new SpinnerNumberModel(3, 0, 100000, 1));
    threadSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 16, 1));

    modeCards = new CardLayout();
    gapCards = new CardLayout();
    modePanel = new JPanel(modeCards);
    gapPanel = new JPanel(gapCards);

    add(buildContentPanel(), BorderLayout.CENTER);
    wireModeSwitching();
  }

  /** Sets visible gap controls for the chosen algorithm label. */
  public void setAlgorithm(String algorithmName) {
    if ("Global Affine".equals(algorithmName)) {
      gapCards.show(gapPanel, GAP_AFFINE);
    } else {
      gapCards.show(gapPanel, GAP_LINEAR);
    }
  }

  /** Returns sequence 1 residues from the active input mode. */
  public String getSequence1() {
    if (isDirectInputMode()) {
      return normalizeSequenceText(sequence1Area.getText());
    }
    if (fasta1Path == null) {
      throw new IllegalArgumentException("Please select FASTA file for sequence 1.");
    }
    return FastaReader.readFirst(fasta1Path).getResidues();
  }

  /** Returns sequence 2 residues from the active input mode. */
  public String getSequence2() {
    if (isDirectInputMode()) {
      return normalizeSequenceText(sequence2Area.getText());
    }
    if (fasta2Path == null) {
      throw new IllegalArgumentException("Please select FASTA file for sequence 2.");
    }
    return FastaReader.readFirst(fasta2Path).getResidues();
  }

  /** Returns the selected score-matrix file path. */
  public Path getMatrixPath() {
    if (matrixPath == null) {
      throw new IllegalArgumentException("Please select a score matrix file.");
    }
    return matrixPath;
  }

  /** Returns the linear gap penalty value. */
  public int getGapPenalty() {
    return ((Number) gapSpinner.getValue()).intValue();
  }

  /** Returns affine gap-opening penalty alpha. */
  public int getAlpha() {
    return ((Number) alphaSpinner.getValue()).intValue();
  }

  /** Returns affine gap-extension penalty beta. */
  public int getBeta() {
    return ((Number) betaSpinner.getValue()).intValue();
  }

  /** Returns configured worker thread count. */
  public int getThreadCount() {
    return ((Number) threadSpinner.getValue()).intValue();
  }

  /** Returns whether direct text mode is currently selected. */
  public boolean isDirectInputMode() {
    return directInputButton.isSelected();
  }

  /** Builds the vertically stacked content sections with titled borders. */
  private JPanel buildContentPanel() {
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(buildSequenceSection());
    content.add(Box.createVerticalStrut(10));
    content.add(buildScoringSection());
    return content;
  }

  /** Creates the sequence input section with mode toggle and mode-specific cards. */
  private JPanel buildSequenceSection() {
    JPanel sequenceSection = new JPanel(new BorderLayout(8, 8));
    sequenceSection.setBorder(BorderFactory.createTitledBorder("Input Sequences"));

    JPanel modeSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    ButtonGroup modeGroup = new ButtonGroup();
    modeGroup.add(directInputButton);
    modeGroup.add(fastaFileButton);
    modeSelectorPanel.add(directInputButton);
    modeSelectorPanel.add(fastaFileButton);
    sequenceSection.add(modeSelectorPanel, BorderLayout.NORTH);

    modePanel.add(buildDirectInputCard(), MODE_DIRECT);
    modePanel.add(buildFastaInputCard(), MODE_FASTA);
    sequenceSection.add(modePanel, BorderLayout.CENTER);
    return sequenceSection;
  }

  /** Builds direct-sequence text input card. */
  private JPanel buildDirectInputCard() {
    JPanel directPanel = new JPanel(new GridLayout(1, 2, 8, 8));
    directPanel.add(buildLabeledTextArea("Sequence 1", sequence1Area));
    directPanel.add(buildLabeledTextArea("Sequence 2", sequence2Area));
    return directPanel;
  }

  /** Builds FASTA file picker card for two input files. */
  private JPanel buildFastaInputCard() {
    JPanel fastaPanel = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0;

    JButton fasta1Button = new JButton("Browse...");
    fasta1Button.addActionListener(e -> chooseFastaFile(true));
    JButton fasta2Button = new JButton("Browse...");
    fasta2Button.addActionListener(e -> chooseFastaFile(false));

    c.gridx = 0;
    c.gridy = 0;
    fastaPanel.add(new JLabel("Sequence 1 FASTA:"), c);
    c.gridx = 1;
    c.weightx = 1;
    fastaPanel.add(fasta1PathLabel, c);
    c.gridx = 2;
    c.weightx = 0;
    fastaPanel.add(fasta1Button, c);

    c.gridx = 0;
    c.gridy = 1;
    fastaPanel.add(new JLabel("Sequence 2 FASTA:"), c);
    c.gridx = 1;
    c.weightx = 1;
    fastaPanel.add(fasta2PathLabel, c);
    c.gridx = 2;
    c.weightx = 0;
    fastaPanel.add(fasta2Button, c);
    return fastaPanel;
  }

  /** Creates scoring and execution-parameter section. */
  private JPanel buildScoringSection() {
    JPanel scoringSection = new JPanel(new GridBagLayout());
    scoringSection.setBorder(BorderFactory.createTitledBorder("Scoring Parameters"));

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(4, 4, 4, 4);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.WEST;

    JButton matrixButton = new JButton("Browse...");
    matrixButton.addActionListener(e -> chooseMatrixFile());

    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0;
    scoringSection.add(new JLabel("Matrix file:"), c);
    c.gridx = 1;
    c.weightx = 1;
    scoringSection.add(matrixPathLabel, c);
    c.gridx = 2;
    c.weightx = 0;
    scoringSection.add(matrixButton, c);

    gapPanel.add(buildLinearGapPanel(), GAP_LINEAR);
    gapPanel.add(buildAffineGapPanel(), GAP_AFFINE);

    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0;
    scoringSection.add(new JLabel("Gap model parameters:"), c);
    c.gridx = 1;
    c.gridwidth = 2;
    c.weightx = 1;
    scoringSection.add(gapPanel, c);
    c.gridwidth = 1;

    c.gridx = 0;
    c.gridy = 2;
    c.weightx = 0;
    scoringSection.add(new JLabel("Threads:"), c);
    c.gridx = 1;
    c.weightx = 0;
    scoringSection.add(threadSpinner, c);

    return scoringSection;
  }

  /** Linear gap controls card. */
  private JPanel buildLinearGapPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    panel.add(new JLabel("Gap penalty:"));
    panel.add(gapSpinner);
    return panel;
  }

  /** Affine gap controls card. */
  private JPanel buildAffineGapPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    panel.add(new JLabel("Alpha:"));
    panel.add(alphaSpinner);
    panel.add(new JLabel("Beta:"));
    panel.add(betaSpinner);
    return panel;
  }

  /** Wires radio-button selection to the input-mode card layout. */
  private void wireModeSwitching() {
    directInputButton.addActionListener(e -> modeCards.show(modePanel, MODE_DIRECT));
    fastaFileButton.addActionListener(e -> modeCards.show(modePanel, MODE_FASTA));
    modeCards.show(modePanel, MODE_DIRECT);
    gapCards.show(gapPanel, GAP_LINEAR);
  }

  /** Builds a labeled scrolling text area used for direct sequence entry. */
  private static JPanel buildLabeledTextArea(String label, JTextArea area) {
    JPanel panel = new JPanel(new BorderLayout(4, 4));
    panel.add(new JLabel(label), BorderLayout.NORTH);
    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(200, 140));
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  /** Creates a monospaced sequence-entry text area. */
  private static JTextArea buildSequenceArea() {
    JTextArea area = new JTextArea(8, 35);
    area.setLineWrap(true);
    area.setWrapStyleWord(true);
    return area;
  }

  /** Opens a FASTA chooser and stores the selected path for sequence input. */
  private void chooseFastaFile(boolean firstSequence) {
    JFileChooser chooser = new JFileChooser(Path.of(".").toAbsolutePath().normalize().toFile());
    chooser.setDialogTitle(firstSequence ? "Select FASTA for Sequence 1" : "Select FASTA for Sequence 2");
    chooser.setFileFilter(new FileNameExtensionFilter("FASTA files (*.fa, *.fasta)", "fa", "fasta"));

    int choice = chooser.showOpenDialog(this);
    if (choice == JFileChooser.APPROVE_OPTION) {
      Path selectedPath = chooser.getSelectedFile().toPath();
      if (firstSequence) {
        fasta1Path = selectedPath;
        fasta1PathLabel.setText(selectedPath.toString());
      } else {
        fasta2Path = selectedPath;
        fasta2PathLabel.setText(selectedPath.toString());
      }
    }
  }

  /** Opens a matrix chooser and stores the selected score-matrix path. */
  private void chooseMatrixFile() {
    JFileChooser chooser = new JFileChooser(Path.of(".").toAbsolutePath().normalize().toFile());
    chooser.setDialogTitle("Select score matrix file");
    chooser.setFileFilter(new FileNameExtensionFilter("Matrix files (*.txt)", "txt"));

    int choice = chooser.showOpenDialog(this);
    if (choice == JFileChooser.APPROVE_OPTION) {
      matrixPath = chooser.getSelectedFile().toPath();
      matrixPathLabel.setText(matrixPath.toString());
    }
  }

  /** Removes whitespace and normalizes residues to uppercase for direct input mode. */
  private static String normalizeSequenceText(String rawText) {
    String normalized = rawText == null ? "" : rawText.replaceAll("\\s+", "");
    return normalized.toUpperCase(Locale.ROOT);
  }
}
