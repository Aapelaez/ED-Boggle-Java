package gui.panels;

import gui.gui_logic.controllers.MainMenuController;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    public interface MenuActions {
        void onJugar();
        void onVerPuntuaciones();
        void onOpciones();
        void onSalir();
    }

    private final JLabel logoLabel = new JLabel("BOGGLE", SwingConstants.CENTER);
    private final JButton btnIniciar = new JButton("Jugar");
    private final JButton btnPuntuaciones = new JButton("Ranking");
    private final JButton btnOpciones = new JButton("Opciones");
    private final JButton btnSalir = new JButton("Salir");

    private MainMenuController controller;

    public MainMenuPanel(MenuActions actions) {
        this.controller = new MainMenuController(this, actions);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));
        setBackground(Color.WHITE);

        logoLabel.setFont(new Font("Arial", Font.BOLD, 48));
        logoLabel.setForeground(Color.BLACK);
        logoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 40, 0));
        add(logoLabel, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new GridLayout(0, 1, 15, 15));
        center.setBackground(Color.WHITE);

        configurarBoton(btnIniciar, 300, 60);
        configurarBoton(btnPuntuaciones, 300, 60);
        configurarBoton(btnOpciones, 300, 60);
        configurarBoton(btnSalir, 300, 60);

        center.add(btnIniciar);
        center.add(btnPuntuaciones);
        center.add(btnOpciones);
        center.add(btnSalir);

        JPanel buttonContainer = new JPanel(new GridBagLayout());
        buttonContainer.setBackground(Color.WHITE);
        buttonContainer.add(center);
        add(buttonContainer, BorderLayout.CENTER);

        btnIniciar.addActionListener(e -> controller.onJugar());
        btnOpciones.addActionListener(e -> controller.onOpciones());
        btnPuntuaciones.addActionListener(e -> controller.onVerPuntuaciones());
        btnSalir.addActionListener(e -> controller.onSalir());
    }

    private void configurarBoton(JButton boton, int ancho, int alto) {
        boton.setPreferredSize(new Dimension(ancho, alto));
        boton.setMinimumSize(new Dimension(ancho, alto));
        boton.setMaximumSize(new Dimension(ancho, alto));
        boton.setFont(new Font("Arial", Font.BOLD, 18));

        boton.setFocusPainted(false);
        boton.setBackground(new Color(70, 130, 180));
        boton.setForeground(Color.BLACK);
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
}