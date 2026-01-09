package gui.gui_logic.controllers;

import sound.AudioManager;
import sound.BackgroundMusic;
import gui.panels.OptionsPanel;

/**
 * Controlador para OptionsPanel - Maneja la lógica de las opciones
 */
public class OptionsController {

    private final OptionsPanel view;
    private final OptionsPanel.OptionActions actions;

    public OptionsController(OptionsPanel view, OptionsPanel.OptionActions actions) {
        this.view = view;
        this.actions = actions;
    }

    public void onVolverMenu() {
        AudioManager.playClick();
        actions.onVolverMenu();
    }

    public void onToggleAudio(boolean enabled) {
        AudioManager.playClick();
        actions.onToggleAudio(enabled);
    }

    public void onToggleMusica(boolean enabled) {
        AudioManager.playClick();
        actions.onToggleMusica(enabled);
    }

    public void onTogglePantallaCompleta(boolean enabled) {
        AudioManager.playClick();
        actions.onTogglePantallaCompleta(enabled);
    }

    public void onAjustarVolumenMusica(int volumen) {
        actions.onAjustarVolumenMusica(volumen);
    }

    public void onAjustarVolumenSonidos(int volumen) {
        actions.onAjustarVolumenSonidos(volumen);
    }

    public void actualizarEstadoAudio() {
        boolean audioActivo = AudioManager.isEnabled();
        boolean musicaActiva = BackgroundMusic.isEnabled();

        view.setAudioActivado(audioActivo);
        view.setMusicaActivada(musicaActiva);
        view.setVolumenMusica((int)(BackgroundMusic.getVolume() * 100));
        view.setVolumenSonidos((int)(AudioManager.getVolumen() * 100));
    }
}