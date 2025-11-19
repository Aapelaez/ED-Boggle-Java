package gui;

import logic.BoggleBoard;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class MainMenuPanel extends JPanel {

    public interface MenuActions {
        void onIniciarPartida();
        void onVerPuntuaciones();
        void onSalir();
        void onToggleAudio(boolean enabled);
    }

    private final JLabel logoLabel = new JLabel("BOGGLE", SwingConstants.CENTER);
    private final JButton btnIniciar = new JButton("Iniciar partida");
    private final JButton btnPuntuaciones = new JButton("Ver puntuaciones");
    private final JButton btnSalir = new JButton("Salir");
    private final JToggleButton btnAudio = new JToggleButton("Audio: ON", true);

    public MainMenuPanel(MenuActions actions, Consumer<JLabel> setupLogo) {
        setLayout(new BorderLayout(12, 12));

        // Zona de logo
        logoLabel.setFont(logoLabel.getFont().deriveFont(Font.BOLD, 36f));
        add(logoLabel, BorderLayout.NORTH);
        if (setupLogo != null) setupLogo.accept(logoLabel);

        // Botonera central
        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 1, 10, 10));
        center.add(btnIniciar);
        center.add(btnPuntuaciones);
        center.add(btnSalir);
        add(center, BorderLayout.CENTER);

        // Barra inferior con toggle de audio
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnAudio);
        add(south, BorderLayout.SOUTH);

        // Acciones
        btnIniciar.addActionListener(e -> {
            AudioManager.playClick();
            actions.onIniciarPartida();
        });
        btnPuntuaciones.addActionListener(e -> {
            AudioManager.playClick();
            actions.onVerPuntuaciones();
        });
        btnSalir.addActionListener(e -> {
            AudioManager.playClick();
            actions.onSalir();
        });
        btnAudio.addActionListener(e -> {
            boolean on = btnAudio.isSelected();
            btnAudio.setText(on ? "Audio: ON" : "Audio: OFF");
            actions.onToggleAudio(on);
        });
    }
}