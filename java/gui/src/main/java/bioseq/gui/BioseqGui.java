package bioseq.gui;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * GUI launcher for the BioSeq sequence alignment toolkit.
 *
 * <p>This class configures Swing look and feel and starts the main application window
 * on the Event Dispatch Thread.
 */
public final class BioseqGui {
  /** Utility launcher class; not instantiable. */
  private BioseqGui() {
  }

  /**
   * Program entry point for the GUI application.
   *
   * @param args command-line arguments (currently unused)
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      setSystemLookAndFeel();
      MainFrame frame = new MainFrame();
      frame.setVisible(true);
    });
  }

  /** Applies the platform-native Swing look and feel when available. */
  private static void setSystemLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException
             | InstantiationException
             | IllegalAccessException
             | UnsupportedLookAndFeelException ignored) {
      // Keep default look and feel when system look and feel cannot be applied.
    }
  }
}
