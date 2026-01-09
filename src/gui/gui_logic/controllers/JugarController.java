package gui.gui_logic.controllers;

import sound.AudioManager;
import gui.panels.JugarPanel;
import logic.Jugador;

/**
 * Controlador para JugarPanel - Maneja las acciones del menú de juego
 */
public class JugarController {

    private final JugarPanel view;
    private final JugarPanel.JugarActions actions;

    public JugarController(JugarPanel view, JugarPanel.JugarActions actions) {
        this.view = view;
        this.actions = actions;
    }

    public void onPartidaIndividual() {
        AudioManager.playClick();
        actions.onPartidaIndividual();
    }

    public void onTorneo() {
        AudioManager.playClick();
        actions.onTorneo();
    }

    public void onPerfiles() {
        AudioManager.playClick();
        actions.onPerfiles();
    }

    public void onVolverMenu() {
        AudioManager.playClick();
        actions.onVolverMenu();
    }

    public void actualizarJugadorActual(Jugador jugador) {
        view.setJugadorActual(jugador);
    }
}