package gui.gui_logic.managers;

import gui.frames.MainFrame;
import logic.Dictionary;
import logic.Partida;
import logic.TrieDictionary;
import utils.DictionaryLoader;

import java.io.InputStream;

/**
 * Gestor simplificado de navegación sin referencias circulares
 */
public class NavigationManager {
    private final PanelManager panelManager;
    private final MainFrame mainFrame;

    public NavigationManager(PanelManager panelManager, MainFrame mainFrame) {
        this.panelManager = panelManager;
        this.mainFrame = mainFrame;
    }

    public void initializePanels() {
        // Paneles se inicializarán bajo demanda
    }

    // Navegación básica
    public void goToMenu() {
        panelManager.showPanel("menu");
    }

    public void goToJugar() {
        panelManager.showPanel("jugar");
    }

    public void goToPerfiles() {
        System.out.println("Navegando a Perfiles...");
        panelManager.showPanel("perfiles");
    }

    public void goToGame(Partida partida) {
        // Esta funcionalidad ahora está en MainFrameController
        System.out.println("Navegación a juego manejada por MainFrameController");
    }

    public void goToOptions() {
        if (panelManager.hasPanel("options")) {
            panelManager.showPanel("options");
        }
    }

    public void goToScoreboard() {
        if (panelManager.hasPanel("scoreboard")) {
            panelManager.showPanel("scoreboard");
        }
    }

    // Métodos de diálogo modal simplificados
    public void showScoreboardDialog() {
        // Esta funcionalidad ahora está en MainFrameController
        System.out.println("Mostrar ranking manejado por MainFrameController");
    }

    public void showOptionsDialog() {
        // Esta funcionalidad ahora está en MainFrameController
        System.out.println("Mostrar opciones manejado por MainFrameController");
    }

    // Método auxiliar para crear diccionario
    public static Dictionary crearDiccionario() {
        TrieDictionary diccionario = new TrieDictionary();

        String[] posiblesRutas = {
                "diccionario.txt",
                "game_files/diccionario.txt",
                "/diccionario.txt",
                "resources/diccionario.txt"
        };

        boolean cargado = false;
        for (String ruta : posiblesRutas) {
            try {
                InputStream is = NavigationManager.class.getClassLoader().getResourceAsStream(ruta);
                if (is != null) {
                    DictionaryLoader.loadIntoDictionary(ruta, diccionario);
                    System.out.println("Diccionario cargado desde: " + ruta);
                    cargado = true;
                    break;
                }
            } catch (Exception e) {
                // Continuar con la siguiente ruta
            }
        }

        if (!cargado) {
            String[] palabrasBasicas = {"hola", "casa", "mesa", "perro", "gato", "sol"};
            for (String palabra : palabrasBasicas) {
                diccionario.insert(palabra);
            }
            System.out.println("Usando diccionario de emergencia");
        }

        return diccionario;
    }

    // Getters
    public MainFrame getMainFrame() {
        return mainFrame;
    }
}