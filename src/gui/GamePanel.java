package gui;

import logic.BoggleBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Locale;

public class GamePanel extends JPanel {

    public interface GameActions {
        void onTerminarPartida(int puntajeFinal);
        void onVolverMenu();
    }

    private static final int DURACION_SEGUNDOS = 180;

    private final JLabel lblJugador = new JLabel();
    private final JLabel lblTiempo = new JLabel("03:00");
    private final JLabel lblPuntos = new JLabel("Puntos: 0");
    private final JPanel panelTablero = new JPanel(new GridLayout(4, 4, 6, 6));
    private final JTextField txtPalabra = new JTextField();
    private final JButton btnAgregar = new JButton("Agregar");
    private final JLabel lblFeedback = new JLabel(" ");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final JButton btnFinalizar = new JButton("Finalizar");

    private int segundosRestantes = DURACION_SEGUNDOS;
    private int puntos = 0;
    private Timer timer;

    public GamePanel(String nombreJugador, char[][] tablero, GameActions actions, Icon relojArenaGif) {
        setLayout(new BorderLayout(10, 10));

        // Norte: jugador, tiempo, puntaje
        JPanel north = new JPanel(new BorderLayout());
        lblJugador.setText("Jugador: " + nombreJugador);
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
        cargarTablero(tablero);
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
        btnAgregar.addActionListener(this::agregarPalabraDemo);
        txtPalabra.addActionListener(this::agregarPalabraDemo);

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

        // Timer 3 minutos
        iniciarTimer();
    }

    private void cargarTablero(char[][] grid) {
        panelTablero.removeAll();
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 28);
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                JLabel cell = new JLabel(String.valueOf(grid[r][c]).toUpperCase(Locale.ROOT), SwingConstants.CENTER);
                cell.setOpaque(true);
                cell.setBackground(new Color(240, 240, 240));
                cell.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
                cell.setFont(f);
                panelTablero.add(cell);
            }
        }
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

    // Modo demo: solo verifica longitud y suma puntos por longitud como referencia Boggle
    private void agregarPalabraDemo(ActionEvent e) {
        AudioManager.playClick();
        String w = txtPalabra.getText().trim().toLowerCase(Locale.ROOT);
        if (w.isEmpty()) return;

        // Validaciones demo (luego se reemplaza por tu lógica Partida)
        String feedback;
        int puntosGanados = 0;
        if (w.length() < 3) {
            feedback = "Muy corta (>=3)";
            lblFeedback.setForeground(new Color(160, 40, 40));
        } else {
            puntosGanados = puntuarPorLongitud(w.length());
            puntos += puntosGanados;
            lblPuntos.setText("Puntos: " + puntos);
            feedback = "OK (+ " + puntosGanados + ")";
            lblFeedback.setForeground(new Color(30, 90, 30));
        }

        lblFeedback.setText(feedback);
        txtPalabra.setText("");
        txtPalabra.requestFocusInWindow();
    }

    private int puntuarPorLongitud(int len) {
        int puntos = 0;
        switch (len) {
            case 0:
            case 1:
            case 2:
                puntos = 0; break;
            case 3:
            case 4:
                puntos = 1; break;
            case 5:
                puntos = 2; break;
            case 6:
                puntos = 3; break;
            case 7:
                puntos = 5; break;
            default:
                if (len >= 8) puntos = 11;
        }
        return puntos;
    }

    private void finalizar(GameActions actions) {
        if (timer != null) timer.stop();
        JOptionPane.showMessageDialog(this, "Tiempo finalizado.\nPuntuación: " + puntos, "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
        actions.onTerminarPartida(puntos);
    }
}