package gui.panels;

import gui.gui_logic.controllers.ScoreboardController;
import logic.Jugador;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ScoreboardPanel extends JPanel {

    public interface ScoreActions {
        void onVolverMenu();
    }

    private final DefaultListModel<String> modelo = new DefaultListModel<>();
    private final JList<String> lista = new JList<>(modelo);
    private final JButton btnVolver = new JButton("Volver");

    private ScoreboardController controller;

    public ScoreboardPanel(ScoreActions actions) {
        this.controller = new ScoreboardController(this, actions);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel titulo = new JLabel("Top 10", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 22f));
        add(titulo, BorderLayout.NORTH);

        add(new JScrollPane(lista), BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.add(btnVolver);
        add(south, BorderLayout.SOUTH);

        btnVolver.addActionListener(e -> controller.onVolverMenu());
    }

    // Mostrar lista real de jugadores
    public void setJugadores(List<Jugador> jugadores) {
        controller.setJugadores(jugadores);
    }

    // Mostrar un único mensaje (por ejemplo, errores o "sin datos")
    public void setMensaje(String msg) {
        controller.setMensaje(msg);
    }

    // Getter para el modelo (necesario para el controlador)
    public DefaultListModel<String> getModelo() {
        return modelo;
    }
}