package gui.gui_logic.controllers;

import sound.AudioManager;
import gui.panels.GamePanel;
import logic.BoggleBoard;
import logic.Partida;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Controlador para GamePanel - Maneja la lógica del juego
 */
public class GameController {

    private static final int DURACION_SEGUNDOS = 180;
    private static final Color HIGHLIGHT_BG = new Color(255, 230, 153);
    private static final Color DEFAULT_BG = new Color(240, 240, 240);

    private final GamePanel view;
    private final Partida partida;
    private final GamePanel.GameActions actions;

    private int segundosRestantes = DURACION_SEGUNDOS;
    private Timer timer;
    private Timer highlightTimer;

    public GameController(GamePanel view, Partida partida, GamePanel.GameActions actions) {
        this.view = view;
        this.partida = partida;
        this.actions = actions;
    }

    public void iniciarTimer() {
        actualizarTiempo();
        if (timer != null && timer.isRunning()) timer.stop();
        timer = new Timer(1000, e -> {
            segundosRestantes--;
            actualizarTiempo();
            if (segundosRestantes <= 0) {
                timer.stop();
                view.setAgregarEnabled(false);
                view.setTxtPalabraEnabled(false);
                view.finalizarPartida();
            }
        });
        timer.start();
    }

    private void actualizarTiempo() {
        int m = segundosRestantes / 60;
        int s = segundosRestantes % 60;
        view.setTiempo(String.format("%02d:%02d", m, s));
    }

    public void agregarPalabra(ActionEvent e) {
        AudioManager.playClick();
        String raw = view.getTxtPalabraText();
        if (raw == null) return;
        view.setTxtPalabraText("");
        view.requestTxtPalabraFocus();

        Partida.ResultadoEnvio res;
        try {
            res = partida.enviarPalabra(raw);
        } catch (IllegalStateException ex) {
            view.setFeedback("La partida ya finalizó.", new Color(160, 40, 40));
            return;
        } catch (Exception ex) {
            view.setFeedback("Error validando palabra.", new Color(160, 40, 40));
            return;
        }

        switch (res.estado) {
            case OK:
                view.setFeedback(String.format("OK: %s (+%d)", res.normalizada, res.puntosGanados),
                        new Color(30, 90, 30));
                int[] ruta = partida.obtenerRutaPalabra(res.normalizada);
                if (ruta != null) {
                    highlightPath(ruta);
                }
                break;
            case MUY_CORTA:
                view.setFeedback("Muy corta", new Color(160, 40, 40));
                break;
            case CARACTERES_INVALIDOS:
                view.setFeedback("Caracteres inválidos", new Color(160, 40, 40));
                break;
            case NO_FORMABLE_EN_TABLERO:
                view.setFeedback("No se puede formar en el tablero", new Color(160, 40, 40));
                break;
            case NO_EN_DICCIONARIO:
                view.setFeedback("No está en el diccionario", new Color(160, 40, 40));
                break;
            case REPETIDA:
                view.setFeedback("Palabra repetida", new Color(160, 40, 40));
                break;
            default:
                view.setFeedback("Error", new Color(160, 40, 40));
                break;
        }

        view.setPuntos("Puntos: " + partida.getPuntosTotales());
    }

    private void highlightPath(int[] ruta) {
        if (highlightTimer != null && highlightTimer.isRunning()) {
            highlightTimer.stop();
        }
        view.clearHighlights();

        for (int idx : ruta) {
            if (idx < 0 || idx >= 16) continue;
            int r = idx / BoggleBoard.COLS;
            int c = idx % BoggleBoard.COLS;
            view.highlightCell(r, c, HIGHLIGHT_BG);
        }

        highlightTimer = new Timer(1200, ev -> {
            view.clearHighlights();
            highlightTimer.stop();
        });
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }

    public void finalizar(GamePanel.GameActions actions) {
        if (timer != null) timer.stop();
        view.setAgregarEnabled(false);
        view.setTxtPalabraEnabled(false);
        int puntosPartida = partida.getPuntosTotales();
        JOptionPane.showMessageDialog(view, "Tiempo finalizado.\nPuntuación de la partida: " + puntosPartida,
                "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
        actions.onTerminarPartida(puntosPartida);
    }

    public void cancelarPartida() {
        AudioManager.playClick();
        if (timer != null) timer.stop();
        int opt = JOptionPane.showConfirmDialog(view, "¿Cancelar partida y volver al menú?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            actions.onVolverMenu();
        } else {
            iniciarTimer();
        }
    }

    public Partida getPartida() {
        return partida;
    }
}