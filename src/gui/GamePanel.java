package gui;

import logic.BoggleBoard;
import logic.Partida;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;

/**
 * Panel de juego conectado con la lógica (Partida).
 * - Muestra tablero a partir de partida.getTablero()
 * - Valida palabras usando partida.enviarPalabra(...)
 * - Muestra feedback y puntos de la partida actual
 * - Resalta temporalmente las celdas usadas por una palabra válida
 */
public class GamePanel extends JPanel {

    public interface GameActions {
        void onTerminarPartida(int puntajeFinal);
        void onVolverMenu();
    }

    private static final int DURACION_SEGUNDOS = 180;
    private static final Color HIGHLIGHT_BG = new Color(255, 230, 153);
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
    private Timer highlightTimer;

    private int segundosRestantes = DURACION_SEGUNDOS;
    private Timer timer;
    private final Partida partida;

    public GamePanel(Partida partida, GameActions actions, Icon relojArenaGif) {
        if (partida == null) throw new IllegalArgumentException("partida no puede ser null");
        this.partida = partida;

        setLayout(new BorderLayout(10, 10));

        // Norte: jugador, tiempo, puntaje
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

        // Centro: tablero
        panelTablero.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cargarTablero(partida.getTablero().getGrid());
        add(panelTablero, BorderLayout.CENTER);

        // Sur: entrada de palabras y feedback
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

        // Acciones
        btnAgregar.addActionListener(this::agregarPalabra);
        txtPalabra.addActionListener(this::agregarPalabra);

        btnCancelar.addActionListener(e -> {
            AudioManager.playClick();
            if (timer != null) timer.stop();
            int opt = JOptionPane.showConfirmDialog(this, "¿Cancelar partida y volver al menú?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                actions.onVolverMenu();
            } else {
                iniciarTimer(); // reanudar
            }
        });

        btnFinalizar.addActionListener(e -> {
            AudioManager.playClick();
            finalizar(actions);
        });

        // Iniciar la partida y el timer
        partida.iniciar();
        iniciarTimer();

        // Inicializar puntos desde la partida (por si hay valores)
        lblPuntos.setText("Puntos: " + partida.getPuntosTotales());
    }

    private void cargarTablero(char[][] grid) {
        panelTablero.removeAll();
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 28);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                JLabel cell = new JLabel(String.valueOf(grid[r][c]).toUpperCase(Locale.ROOT), SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(DEFAULT_BG);
                cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                cell.setFont(f);
                cell.setPreferredSize(new Dimension(64, 64));
                panelTablero.add(cell);
                cellLabels[r][c] = cell;
            }
        }
        panelTablero.revalidate();
        panelTablero.repaint();
    }

    private void iniciarTimer() {
        actualizarTiempo();
        if (timer != null && timer.isRunning()) timer.stop();
        timer = new Timer(1000, e -> {
            segundosRestantes--;
            actualizarTiempo();
            if (segundosRestantes <= 0) {
                timer.stop();
                btnAgregar.setEnabled(false);
                txtPalabra.setEnabled(false);
                btnFinalizar.doClick();
            }
        });
        timer.start();
    }

    private void actualizarTiempo() {
        int m = segundosRestantes / 60;
        int s = segundosRestantes % 60;
        lblTiempo.setText(String.format("%02d:%02d", m, s));
    }

    // Integración con Partida: enviar palabra y procesar ResultadoEnvio
    private void agregarPalabra(ActionEvent e) {
        AudioManager.playClick();
        String raw = txtPalabra.getText();
        if (raw == null) return;
        txtPalabra.setText("");
        txtPalabra.requestFocusInWindow();

        Partida.ResultadoEnvio res;
        try {
            res = partida.enviarPalabra(raw);
        } catch (IllegalStateException ex) {
            lblFeedback.setText("La partida ya finalizó.");
            lblFeedback.setForeground(new Color(160, 40, 40));
            return;
        } catch (Exception ex) {
            lblFeedback.setText("Error validando palabra.");
            lblFeedback.setForeground(new Color(160, 40, 40));
            return;
        }

        switch (res.estado) {
            case OK:
                lblFeedback.setText(String.format("OK: %s (+%d)", res.normalizada, res.puntosGanados));
                lblFeedback.setForeground(new Color(30, 90, 30));
                // Obtener ruta y resaltar celdas
                int[] ruta = partida.obtenerRutaPalabra(res.normalizada);
                if (ruta != null) {
                    highlightPath(ruta);
                }
                break;
            case MUY_CORTA:
                lblFeedback.setText("Muy corta");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
            case CARACTERES_INVALIDOS:
                lblFeedback.setText("Caracteres inválidos");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
            case NO_FORMABLE_EN_TABLERO:
                lblFeedback.setText("No se puede formar en el tablero");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
            case NO_EN_DICCIONARIO:
                lblFeedback.setText("No está en el diccionario");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
            case REPETIDA:
                lblFeedback.setText("Palabra repetida");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
            default:
                lblFeedback.setText("Error");
                lblFeedback.setForeground(new Color(160, 40, 40));
                break;
        }

        // Actualizar puntos de la PARTIDA
        lblPuntos.setText("Puntos: " + partida.getPuntosTotales());
    }

    // Resalta temporalmente las celdas indicadas (índices 0..15). Borra resaltados previos.
    private void highlightPath(int[] ruta) {
        // cancelar timer anterior si está activo y limpiar
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.stop();
        }
        clearHighlights();

        // Pintar nuevo resaltado
        for (int idx : ruta) {
            if (idx < 0 || idx >= 16) continue;
            int r = idx / BoggleBoard.COLS;
            int c = idx % BoggleBoard.COLS;
            JLabel cell = cellLabels[r][c];
            cell.setBackground(HIGHLIGHT_BG);
            cell.setBorder(BorderFactory.createLineBorder(new Color(200, 120, 0), 2));
        }

        // Programar limpieza en 1.2s
        highlightTimer = new Timer(1200, ev -> {
            clearHighlights();
            highlightTimer.stop();
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }

    private void clearHighlights() {
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

    private void finalizar(GameActions actions) {
        if (timer != null) timer.stop();
        btnAgregar.setEnabled(false);
        txtPalabra.setEnabled(false);
        int puntosPartida = partida.getPuntosTotales();
        JOptionPane.showMessageDialog(this, "Tiempo finalizado.\nPuntuación de la partida: " + puntosPartida, "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
        actions.onTerminarPartida(puntosPartida);
    }
}