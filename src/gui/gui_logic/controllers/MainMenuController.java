package gui.gui_logic.controllers;

import sound.AudioManager;
import gui.panels.MainMenuPanel;

/**
 * Controlador para MainMenuPanel - Maneja las acciones del menú principal
 */
public class MainMenuController {

    private  MainMenuPanel view;
    private final MainMenuPanel.MenuActions actions;

    public MainMenuController(MainMenuPanel view, MainMenuPanel.MenuActions actions) {
        this.view = view;
        this.actions = actions;
    }

    public void onJugar() {
        AudioManager.playClick();
        actions.onJugar();
    }

    public void onVerPuntuaciones() {
        AudioManager.playClick();
        actions.onVerPuntuaciones();
    }

    public void onOpciones() {
        AudioManager.playClick();
        actions.onOpciones();
    }

    public void onSalir() {
        AudioManager.playClick();
        actions.onSalir();
    }

    public MainMenuPanel getView() {
        return view;
    }

    public void setView(MainMenuPanel view) {
        this.view = view;
    }


}