package gui.panels;

import gui.gui_logic.controllers.OptionsController;
import sound.AudioManager;
import sound.BackgroundMusic;

import javax.swing.*;
import java.awt.*;

public class OptionsPanel extends JPanel {

    public interface OptionActions {
        void onVolverMenu();
        void onToggleAudio(boolean enabled);
        void onToggleMusica(boolean enabled);
        void onTogglePantallaCompleta(boolean enabled);
        void onAjustarVolumenMusica(int volumen);
        void onAjustarVolumenSonidos(int volumen);
    }

    private final JButton btnVolver = new JButton("Volver al Menú");
    private final JToggleButton btnAudio = new JToggleButton("Sonidos: ON");
    private final JToggleButton btnMusica = new JToggleButton("Música: ON");
    private final JToggleButton btnPantallaCompleta = new JToggleButton("Pantalla Completa: OFF");

    private final JSlider sliderVolumenMusica = new JSlider(0, 100, 60);
    private final JSlider sliderVolumenSonidos = new JSlider(0, 100, 75);

    private OptionsController controller;

    public OptionsPanel(OptionActions actions) {
        this.controller = new OptionsController(this, actions);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titulo = new JLabel("OPCIONES", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(0, 82, 155));
        add(titulo, BorderLayout.NORTH);

        JPanel panelControles = new JPanel();
        panelControles.setLayout(new GridLayout(0, 1, 15, 15));
        panelControles.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JPanel panelAudio = crearPanelControl("Sonidos del Juego:", btnAudio, sliderVolumenSonidos);
        JPanel panelMusica = crearPanelControl("Música de Fondo:", btnMusica, sliderVolumenMusica);

        JPanel panelPantalla = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelPantalla.add(new JLabel("Pantalla Completa:"), BorderLayout.WEST);
        panelPantalla.add(btnPantallaCompleta, BorderLayout.CENTER);

        panelControles.add(panelAudio);
        panelControles.add(panelMusica);
        panelControles.add(panelPantalla);
        panelControles.add(new JSeparator());

        add(panelControles, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSur.add(btnVolver);
        add(panelSur, BorderLayout.SOUTH);

        btnAudio.setSelected(true);
        btnMusica.setSelected(true);
        btnPantallaCompleta.setSelected(false);

        sliderVolumenMusica.setMajorTickSpacing(25);
        sliderVolumenMusica.setMinorTickSpacing(5);
        sliderVolumenMusica.setPaintTicks(true);
        sliderVolumenMusica.setPaintLabels(true);

        sliderVolumenSonidos.setMajorTickSpacing(25);
        sliderVolumenSonidos.setMinorTickSpacing(5);
        sliderVolumenSonidos.setPaintTicks(true);
        sliderVolumenSonidos.setPaintLabels(true);

        btnVolver.addActionListener(e -> controller.onVolverMenu());

        btnAudio.addActionListener(e -> {
            boolean activado = btnAudio.isSelected();
            btnAudio.setText(activado ? "Sonidos: ON" : "Sonidos: OFF");
            controller.onToggleAudio(activado);
        });

        btnMusica.addActionListener(e -> {
            boolean activado = btnMusica.isSelected();
            btnMusica.setText(activado ? "Música: ON" : "Música: OFF");
            controller.onToggleMusica(activado);
        });

        btnPantallaCompleta.addActionListener(e -> {
            boolean activado = btnPantallaCompleta.isSelected();
            btnPantallaCompleta.setText(activado ? "Pantalla Completa: ON" : "Pantalla Completa: OFF");
            controller.onTogglePantallaCompleta(activado);
        });

        sliderVolumenMusica.addChangeListener(e -> {
            if (!sliderVolumenMusica.getValueIsAdjusting()) {
                controller.onAjustarVolumenMusica(sliderVolumenMusica.getValue());
            }
        });

        sliderVolumenSonidos.addChangeListener(e -> {
            if (!sliderVolumenSonidos.getValueIsAdjusting()) {
                controller.onAjustarVolumenSonidos(sliderVolumenSonidos.getValue());
            }
        });

        controller.actualizarEstadoAudio();
    }

    private JPanel crearPanelControl(String titulo, JToggleButton boton, JSlider slider) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.add(new JLabel(titulo), BorderLayout.WEST);
        panelSuperior.add(boton, BorderLayout.CENTER);

        panel.add(panelSuperior, BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);

        return panel;
    }

    // Métodos para sincronizar el estado desde fuera
    public void setAudioActivado(boolean activado) {
        btnAudio.setSelected(activado);
        btnAudio.setText(activado ? "Sonidos: ON" : "Sonidos: OFF");
        AudioManager.setEnabled(activado);
    }

    public void setMusicaActivada(boolean activada) {
        btnMusica.setSelected(activada);
        btnMusica.setText(activada ? "Música: ON" : "Música: OFF");
        BackgroundMusic.setEnabled(activada);
    }

    public void setPantallaCompleta(boolean activada) {
        btnPantallaCompleta.setSelected(activada);
        btnPantallaCompleta.setText(activada ? "Pantalla Completa: ON" : "Pantalla Completa: OFF");
    }

    public void setVolumenMusica(int volumen) {
        sliderVolumenMusica.setValue(volumen);
    }

    public void setVolumenSonidos(int volumen) {
        sliderVolumenSonidos.setValue(volumen);
    }

    // Método para forzar actualización visual
    public void actualizarEstadoAudio() {
        controller.actualizarEstadoAudio();
    }
}