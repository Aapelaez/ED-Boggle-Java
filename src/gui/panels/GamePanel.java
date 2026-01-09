package gui.panels;

import gui.gui_logic.controllers.GameController;
import logic.Partida;
import sound.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;

/**
 * Panel de juego (solo interfaz gráfica)
 */
public class GamePanel extends JPanel {

    public interface GameActions {
        void onTerminarPartida(int puntajeFinal);
        void onVolverMenu();
    }

    private static final Color DEFAULT_BG = new Color(240, 240, 240);

    private final JLabel lblJugador = new JLabel();
    private final JLabel lblTiempo = new JLabel("03:00");
    private final JLabel lblPuntos = new JLabel("Puntos: 0");
    private final JPanel panelTablero = new JPanel(new GridLayout(4, 4, 6, 6));
    private final JTextField txtPalabra = new JTextField();
    private final JButton btnAgregar = new JButton("Agregar");
    private final JLabel lblFeedback = new JLabel(" ");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnFinalizar = new JButton("Finalizar");

    private final JLabel[][] cellLabels = new JLabel[4][4];

    private final Partida partida;
    private final GameController controller;

    public GamePanel(Partida partida, GameActions actions, Icon relojArenaGif) {
        if (partida == null) throw new IllegalArgumentException("partida no puede ser null");
        this.partida = partida;
        this.controller = new GameController(this, partida, actions);

        initUI(relojArenaGif, actions);
        partida.iniciar();
        controller.iniciarTimer();
        lblPuntos.setText("Puntos: " + partida.getPuntosTotales());
    }

    private void initUI(Icon relojArenaGif, GameActions actions) {
        setLayout(new BorderLayout(10, 10));

        JPanel north = new JPanel(new BorderLayout());
        lblJugador.setText("Jugador: " + partida.getNombreJugador());
        lblJugador.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        north.add(lblJugador, BorderLayout.WEST);

        JPanel timeScore = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        if (relojArenaGif != null) {
            timeScore.add(new JLabel(relojArenaGif));
        }
        lblTiempo.setFont(lblTiempo.getFont().deriveFont(Font.BOLD, 18f));
        timeScore.add(lblTiempo);
        timeScore.add(lblPuntos);
        north.add(timeScore, BorderLayout.EAST);

        add(north, BorderLayout.NORTH);

        panelTablero.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cargarTablero(partida.getTablero().getGrid());
        add(panelTablero, BorderLayout.CENTER);

        JPanel input = new JPanel(new BorderLayout(8, 8));
        input.setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
        JPanel fieldRow = new JPanel(new BorderLayout(6, 6));
        fieldRow.add(txtPalabra, BorderLayout.CENTER);
        fieldRow.add(btnAgregar, BorderLayout.EAST);
        input.add(fieldRow, BorderLayout.NORTH);

        lblFeedback.setForeground(new Color(30, 90, 30));
        input.add(lblFeedback, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(btnCancelar);
        buttons.add(btnFinalizar);
        input.add(buttons, BorderLayout.SOUTH);

        add(input, BorderLayout.SOUTH);

        btnAgregar.addActionListener(controller::agregarPalabra);
        txtPalabra.addActionListener(controller::agregarPalabra);

        btnCancelar.addActionListener(e -> controller.cancelarPartida());

        btnFinalizar.addActionListener(e -> {
            AudioManager.playClick();
            controller.finalizar(actions);
        });
    }

    private void cargarTablero(char[][] grid) {
        panelTablero.removeAll();
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 28);

        Dimension cellSize = new Dimension(80, 80);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                JLabel cell = new JLabel(String.valueOf(grid[r][c]).toUpperCase(Locale.ROOT), SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(DEFAULT_BG);
                cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                cell.setFont(f);
                cell.setPreferredSize(cellSize);
                cell.setMinimumSize(cellSize);
                cell.setMaximumSize(cellSize);
                panelTablero.add(cell);
                cellLabels[r][c] = cell;
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    // Métodos para que el controlador interactúe con la vista
    public void setTiempo(String tiempo) {
        lblTiempo.setText(tiempo);
    }

    public void setPuntos(String puntos) {
        lblPuntos.setText(puntos);
    }

    public void setFeedback(String mensaje, Color color) {
        lblFeedback.setText(mensaje);
        lblFeedback.setForeground(color);
    }

    public String getTxtPalabraText() {
        return txtPalabra.getText();
    }

    public void setTxtPalabraText(String text) {
        txtPalabra.setText(text);
    }

    public void requestTxtPalabraFocus() {
        txtPalabra.requestFocusInWindow();
    }

    public void setAgregarEnabled(boolean enabled) {
        btnAgregar.setEnabled(enabled);
    }

    public void setTxtPalabraEnabled(boolean enabled) {
        txtPalabra.setEnabled(enabled);
    }

    public void finalizarPartida() {
        btnFinalizar.doClick();
    }

    public void highlightCell(int r, int c, Color color) {
        JLabel cell = cellLabels[r][c];
        cell.setBackground(color);
        cell.setBorder(BorderFactory.createLineBorder(new Color(200, 120, 0), 2));
    }

    public void clearHighlights() {
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                JLabel cell = cellLabels[r][c];
                if (cell != null) {
                    cell.setBackground(DEFAULT_BG);
                    cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                }
            }
        }
    }
}