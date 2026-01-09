package gui.frames;

import sound.AudioManager;
import gui.panels.*;
import gui.gui_logic.controllers.MainFrameController;
import gui.gui_logic.controllers.OptionsController;
import gui.gui_logic.controllers.ScoreboardController;
import logic.Partida;
import logic.Jugador;
import sound.BackgroundMusic;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación Swing (solo interfaz gráfica)
 */
public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private ScoreboardPanel scoreboardPanel;
    private OptionsPanel optionsPanel;
    private JugarPanel jugarPanel;
    private PerfilesPanel perfilesPanel;

    private JLabel lblJugadorActual;
    private MainFrameController controller;

    public MainFrame() {
        super("Boggle");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 700);
        setMinimumSize(new Dimension(800, 700));
        setMaximumSize(new Dimension(800, 700));
        setResizable(false);
        setLocationRelativeTo(null);

        controller = new MainFrameController(this);
        initUI();

        setContentPane(root);
        cards.show(root, "menu");

        if (controller.isPantallaCompletaActiva()) {
            SwingUtilities.invokeLater(() -> {
                // togglePantallaCompleta será manejado por el controlador
            });
        }

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                controller.guardarEstadoYSalir();
            }
        });
    }

    private void initUI() {
        MainMenuPanel menuPanel = new MainMenuPanel(new MainMenuPanel.MenuActions() {
            @Override
            public void onJugar() {
                AudioManager.playClick();
                mostrarMenuJugar();
            }

            @Override
            public void onVerPuntuaciones() {
                AudioManager.playClick();
                mostrarRanking();  // Cambiado: Ahora muestra dentro de la misma ventana
            }

            @Override
            public void onOpciones() {
                AudioManager.playClick();
                mostrarOpciones();  // Cambiado: Ahora muestra dentro de la misma ventana
            }

            @Override
            public void onSalir() {
                AudioManager.playClick();
                BackgroundMusic.stop();
                System.exit(0);
            }
        });

        root.add(menuPanel, "menu");
        actualizarDisplayJugadorActual();
        BackgroundMusic.playAmbiente();
    }

    public void mostrarMenuJugar() {
        if (jugarPanel == null) {
            jugarPanel = new JugarPanel(new JugarPanel.JugarActions() {
                @Override
                public void onPartidaIndividual() {
                    AudioManager.playClick();
                    controller.flujoRegistroEInicioPartida();
                }

                @Override
                public void onTorneo() {
                    AudioManager.playClick();
                    controller.iniciarTorneo();
                }

                @Override
                public void onPerfiles() {
                    AudioManager.playClick();
                    mostrarPerfiles();
                }

                @Override
                public void onVolverMenu() {
                    AudioManager.playClick();
                    volverMenu();
                }
            });
            root.add(jugarPanel, "jugar");
        }
        actualizarDisplayJugadorActual();
        cards.show(root, "jugar");
    }

    public void mostrarPerfiles() {
        if (perfilesPanel == null) {
            perfilesPanel = new PerfilesPanel(controller.getDatosFile(), () -> {
                AudioManager.playClick();
                mostrarMenuJugar();  // Volver al menú jugar
            }, this);
            root.add(perfilesPanel, "perfiles");
        }
        perfilesPanel.actualizarLista();
        cards.show(root, "perfiles");
    }

    public void mostrarRanking() {
        if (scoreboardPanel == null) {
            scoreboardPanel = new ScoreboardPanel(() -> {
                AudioManager.playClick();
                volverMenu();  // Volver al menú principal
            });
            root.add(scoreboardPanel, "ranking");

            // Inicializar el controlador
            ScoreboardController rankingController = new ScoreboardController(
                    scoreboardPanel,
                    new ScoreboardPanel.ScoreActions() {
                        @Override
                        public void onVolverMenu() {
                            AudioManager.playClick();
                            volverMenu();
                        }
                    }
            );

            // Cargar los jugadores en el ranking
            try {
                rankingController.setJugadores(controller.obtenerTodosJugadores());
            } catch (Exception e) {
                rankingController.setMensaje("Error al cargar el ranking: " + e.getMessage());
            }
        } else {
            // Actualizar la lista de jugadores
            try {
                ScoreboardController rankingController = new ScoreboardController(
                        scoreboardPanel,
                        new ScoreboardPanel.ScoreActions() {
                            @Override
                            public void onVolverMenu() {
                                AudioManager.playClick();
                                volverMenu();
                            }
                        }
                );
                rankingController.setJugadores(controller.obtenerTodosJugadores());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cards.show(root, "ranking");
    }

    public void mostrarOpciones() {
        if (optionsPanel == null) {
            optionsPanel = new OptionsPanel(new OptionsPanel.OptionActions() {
                @Override
                public void onVolverMenu() {
                    AudioManager.playClick();
                    volverMenu();
                }

                @Override
                public void onToggleAudio(boolean enabled) {
                    AudioManager.setEnabled(enabled);
                }

                @Override
                public void onToggleMusica(boolean enabled) {
                    BackgroundMusic.setEnabled(enabled);
                    if (enabled) {
                        BackgroundMusic.playAmbiente();
                    } else {
                        BackgroundMusic.stop();
                    }
                }

                @Override
                public void onTogglePantallaCompleta(boolean enabled) {
                    controller.togglePantallaCompleta(enabled);
                }

                @Override
                public void onAjustarVolumenMusica(int volumen) {
                    BackgroundMusic.setVolume(volumen / 100.0f);
                }

                @Override
                public void onAjustarVolumenSonidos(int volumen) {
                    AudioManager.setVolumen(volumen / 100.0f);
                }
            });
            root.add(optionsPanel, "opciones");

            // Inicializar controlador de opciones
            OptionsController optionsController = new OptionsController(
                    optionsPanel,
                    new OptionsPanel.OptionActions() {
                        @Override
                        public void onVolverMenu() {
                            AudioManager.playClick();
                            volverMenu();
                        }

                        @Override
                        public void onToggleAudio(boolean enabled) {
                            AudioManager.setEnabled(enabled);
                        }

                        @Override
                        public void onToggleMusica(boolean enabled) {
                            BackgroundMusic.setEnabled(enabled);
                            if (enabled) {
                                BackgroundMusic.playAmbiente();
                            } else {
                                BackgroundMusic.stop();
                            }
                        }

                        @Override
                        public void onTogglePantallaCompleta(boolean enabled) {
                            controller.togglePantallaCompleta(enabled);
                        }

                        @Override
                        public void onAjustarVolumenMusica(int volumen) {
                            BackgroundMusic.setVolume(volumen / 100.0f);
                        }

                        @Override
                        public void onAjustarVolumenSonidos(int volumen) {
                            AudioManager.setVolumen(volumen / 100.0f);
                        }
                    }
            );
            optionsController.actualizarEstadoAudio();
        } else {
            // Actualizar el estado de audio/música
            OptionsController optionsController = new OptionsController(
                    optionsPanel,
                    new OptionsPanel.OptionActions() {
                        @Override
                        public void onVolverMenu() {
                            AudioManager.playClick();
                            volverMenu();
                        }

                        @Override
                        public void onToggleAudio(boolean enabled) {
                            AudioManager.setEnabled(enabled);
                        }

                        @Override
                        public void onToggleMusica(boolean enabled) {
                            BackgroundMusic.setEnabled(enabled);
                            if (enabled) {
                                BackgroundMusic.playAmbiente();
                            } else {
                                BackgroundMusic.stop();
                            }
                        }

                        @Override
                        public void onTogglePantallaCompleta(boolean enabled) {
                            controller.togglePantallaCompleta(enabled);
                        }

                        @Override
                        public void onAjustarVolumenMusica(int volumen) {
                            BackgroundMusic.setVolume(volumen / 100.0f);
                        }

                        @Override
                        public void onAjustarVolumenSonidos(int volumen) {
                            AudioManager.setVolumen(volumen / 100.0f);
                        }
                    }
            );
            optionsController.actualizarEstadoAudio();
        }
        cards.show(root, "opciones");
    }

    public void mostrarJuego(Partida partida) {
        controller.mostrarJuego(partida);
    }

    public void mostrarPanel(JPanel panel, String nombre) {
        root.add(panel, nombre);
        cards.show(root, nombre);
    }

    public void volverMenu() {
        cards.show(root, "menu");
    }

    public void actualizarDisplayJugadorActual() {
        if (jugarPanel != null) {
            jugarPanel.actualizarJugadorActual(controller.getJugadorActual());
        }
    }

    public void setJugadorActual(Jugador jugador) {
        controller.setJugadorActual(jugador);
    }

    public Jugador getJugadorActual() {
        return controller.getJugadorActual();
    }

    public void setFullScreenSize(Dimension contentSize) {
        // Método para ajustar tamaño en pantalla completa
    }

    // Método para refrescar el ranking cuando se agregan nuevos jugadores
    public void refrescarRanking() {
        if (scoreboardPanel != null) {
            mostrarRanking();
        }
    }
}