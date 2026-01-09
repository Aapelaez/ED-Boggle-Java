package gui.gui_logic.controllers;

import sound.AudioManager;
import gui.panels.ScoreboardPanel;
import logic.Jugador;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Controlador para ScoreboardPanel - Maneja la lógica del ranking
 */
public class ScoreboardController {

    private final ScoreboardPanel view;
    private final ScoreboardPanel.ScoreActions actions;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public ScoreboardController(ScoreboardPanel view, ScoreboardPanel.ScoreActions actions) {
        this.view = view;
        this.actions = actions;
    }

    public void onVolverMenu() {
        AudioManager.playClick();
        actions.onVolverMenu();
    }

    public void setJugadores(List<Jugador> jugadores) {
        DefaultListModel<String> modelo = view.getModelo();
        modelo.clear();
        int pos = 1;
        for (Jugador j : jugadores) {
            String fecha = formatFecha(j.getUltimaPartida());
            String linea = String.format("%2d. %s - %d pts  (última: %s)",
                    pos++, j.getNombre(), j.getPuntos(), fecha);
            modelo.addElement(linea);
        }
        if (modelo.isEmpty()) {
            modelo.addElement("No hay jugadores registrados todavía.");
        }
    }

    public void setMensaje(String msg) {
        DefaultListModel<String> modelo = view.getModelo();
        modelo.clear();
        modelo.addElement(msg);
    }

    private String formatFecha(Date d) {
        return d == null ? "-" : sdf.format(d);
    }

    
}