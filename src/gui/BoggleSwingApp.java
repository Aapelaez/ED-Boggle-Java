package gui;

import javax.swing.*;

public class BoggleSwingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            // Configura la ruta si usas otra y precarga
            AudioManager.setClickPath("game_files/click.wav");
            AudioManager.preloadClick();

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}