package gui;

import gui.frames.MainFrame;
import sound.AudioManager;
import sound.BackgroundMusic;

import javax.swing.*;

public class BoggleSwingApp {
    public static void iniciarInterfaz(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                // Cargar configuración
                BackgroundMusic.cargarConfiguracionAudio();
                AudioManager.setClickPath("game_files/click.wav");
                AudioManager.preloadClick();

                // Crear ventana principal
                MainFrame frame = new MainFrame();
                frame.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error al iniciar la aplicación:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}