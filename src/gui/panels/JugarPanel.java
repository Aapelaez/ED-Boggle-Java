package gui.panels;

import javax.swing.*;
import java.awt.*;

import gui.gui_logic.controllers.JugarController;
import logic.Jugador;

public class JugarPanel extends JPanel {

    public interface JugarActions {
        void onPartidaIndividual();
        void onTorneo();
        void onPerfiles();
        void onVolverMenu();
    }

    private final JButton btnPartida = new JButton("Partida Individual");
    private final JButton btnTorneo = new JButton("Torneo");
    private final JButton btnPerfiles = new JButton("Perfiles");
    private final JButton btnVolver = new JButton("Volver al Menú");

    private final JLabel lblJugadorActual = new JLabel("No hay jugador seleccionado", SwingConstants.CENTER);

    private JugarController controller;

    public JugarPanel(JugarActions actions) {
        this.controller = new JugarController(this, actions);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(Box.createVerticalStrut(10));

        JLabel titulo = new JLabel("MODOS DE JUEGO", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 36));
        titulo.setForeground(Color.BLACK);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titulo);

        mainPanel.add(Box.createVerticalStrut(15));

        lblJugadorActual.setFont(new Font("Arial", Font.BOLD, 14));
        lblJugadorActual.setForeground(new Color(50, 110, 160));
        lblJugadorActual.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblJugadorActual);

        mainPanel.add(Box.createVerticalStrut(40));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Color.WHITE);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(3, 1, 20, 20));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setMaximumSize(new Dimension(350, 250));

        configurarBoton(btnPartida, 350, 70);
        configurarBoton(btnTorneo, 350, 70);
        configurarBoton(btnPerfiles, 350, 70);

        buttonsPanel.add(btnPartida);
        buttonsPanel.add(btnTorneo);
        buttonsPanel.add(btnPerfiles);

        center.add(buttonsPanel);
        mainPanel.add(center);

        mainPanel.add(Box.createVerticalGlue());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setBackground(Color.WHITE);
        south.setAlignmentX(Component.CENTER_ALIGNMENT);
        configurarBoton(btnVolver, 250, 50);
        south.add(btnVolver);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(south);
        mainPanel.add(Box.createVerticalStrut(10));

        add(mainPanel, BorderLayout.CENTER);

        btnPartida.addActionListener(e -> controller.onPartidaIndividual());
        btnTorneo.addActionListener(e -> controller.onTorneo());
        btnPerfiles.addActionListener(e -> controller.onPerfiles());
        btnVolver.addActionListener(e -> controller.onVolverMenu());
    }

    // Método para actualizar el display del jugador
    public void actualizarJugadorActual(Jugador jugador) {
        if (jugador != null) {
            lblJugadorActual.setText("Jugador: " + jugador.getNombre());
        } else {
            lblJugadorActual.setText("No hay jugador seleccionado");
        }
        lblJugadorActual.revalidate();
        lblJugadorActual.repaint();
    }

    private void configurarBoton(JButton boton, int ancho, int alto) {
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setMinimumSize(new Dimension(ancho, alto));
        boton.setMaximumSize(new Dimension(ancho, alto));
        boton.setFont(new Font("Arial", Font.BOLD, 18));
        boton.setFocusPainted(false);
        boton.setBackground(new Color(70, 130, 180));
        boton.setForeground(Color.BLACK);
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 110, 160), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(90, 150, 200));
                boton.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(70, 130, 180));
                boton.setForeground(Color.BLACK);
            }
        });
    }

    // Setter para el controlador
    public void setJugadorActual(Jugador jugador) {
        if (jugador != null) {
            lblJugadorActual.setText("Jugador: " + jugador.getNombre());
        } else {
            lblJugadorActual.setText("No hay jugador seleccionado");
        }
    }
}