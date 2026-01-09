package gui.gui_logic.controllers;

import sound.AudioManager;
import sound.BackgroundMusic;
import gui.frames.MainFrame;
import gui.panels.GamePanel;
import gui.panels.OptionsPanel;
import logic.TableroBoggle;
import logic.Diccionario;
import logic.Jugador;
import logic.Partida;
import logic.Torneo;
import logic.JugadorTorneo;
import logic.GestorTorneos;
import logic.TorneoUtils;
import logic.ArbolPrefijos;
import utils.DictionaryLoader;
import utils.TrabajarFichero;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.file.*;

/**
 * Controlador para MainFrame - Maneja la lógica de la ventana principal
 */
public class MainFrameController {

    private final MainFrame view;
    private final File datosFile;
    private Jugador jugadorActual;
    private Diccionario dict;
    private boolean pantallaCompletaActiva = false;

    private static final String PREF_JUGADOR_ACTUAL = "jugador_actual";
    private static final String PREF_JUGADOR_NOMBRE = "jugador_nombre";
    private static final String PREF_JUGADOR_PUNTOS = "jugador_puntos";
    private static final String PREF_JUGADOR_PARTIDAS = "jugador_partidas";

    public MainFrameController(MainFrame view) {
        this.view = view;

        // Determina la carpeta de datos del usuario
        File appDataDir = resolveAppDataDirectory();
        migrateLegacyGameFilesIfAny(appDataDir);

        this.datosFile = new File(appDataDir, "datos_partidas.dat");
        inicializarFicheroDatos();

        // Cargar preferencias
        cargarPreferencias();

        // Cargar jugador guardado
        cargarJugadorGuardado();
    }

    private File resolveAppDataDirectory() {
        String override = System.getProperty("boggle.dataDir");
        if (override != null && !override.trim().isEmpty()) {
            File d = new File(override);
            if (!d.exists()) d.mkdirs();
            return d;
        }

        String userHome = System.getProperty("user.home");
        Path p = Paths.get(userHome, ".boggle");
        File dir = p.toFile();
        if (!dir.exists()) {
            try {
                Files.createDirectories(p);
            } catch (IOException e) {
                return new File(System.getProperty("user.dir"));
            }
        }
        return dir;
    }

    private void migrateLegacyGameFilesIfAny(File appDataDir) {
        File maybeDatos = new File(appDataDir, "datos_partidas.dat");
        if (maybeDatos.exists()) return;

        try {
            String codeLocation = MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File codeFile = new File(codeLocation);
            File jarParent = codeFile.isFile() ? codeFile.getParentFile() : codeFile;
            File legacy1 = new File(jarParent, "game_files");

            File cwd = new File(System.getProperty("user.dir"));
            File legacy2 = new File(cwd, "game_files");

            if (legacy1.exists() && legacy1.isDirectory()) {
                copyDirectoryIfNotExists(legacy1.toPath(), appDataDir.toPath());
                return;
            }
            if (legacy2.exists() && legacy2.isDirectory()) {
                copyDirectoryIfNotExists(legacy2.toPath(), appDataDir.toPath());
                return;
            }
        } catch (Exception ignored) {
        }
    }

    private void copyDirectoryIfNotExists(Path srcDir, Path destDir) {
        try {
            if (!Files.exists(destDir)) Files.createDirectories(destDir);
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcDir)) {
                for (Path entry : ds) {
                    Path dest = destDir.resolve(entry.getFileName());
                    if (Files.exists(dest)) continue;
                    Files.copy(entry, dest, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void inicializarFicheroDatos() {
        try {
            File parent = datosFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            if (!datosFile.exists()) datosFile.createNewFile();
            TrabajarFichero.crearEncabezado(datosFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "No se pudo inicializar el fichero de datos:\n" + e.getMessage(),
                    "Error de inicialización", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void cargarPreferenciasEnOptionsPanel(OptionsPanel optionsPanel) {
        try {
            Path prefsPath = Paths.get(resolveAppDataDirectory().getAbsolutePath(), "preferencias.properties");
            if (!Files.exists(prefsPath)) return;

            java.util.Properties props = new java.util.Properties();
            props.load(Files.newInputStream(prefsPath));

            String audio = props.getProperty("audio_habilitado", "true");
            optionsPanel.setAudioActivado(Boolean.parseBoolean(audio));
            AudioManager.setEnabled(Boolean.parseBoolean(audio));

            String musica = props.getProperty("musica_habilitada", "true");
            optionsPanel.setMusicaActivada(Boolean.parseBoolean(musica));
            BackgroundMusic.setEnabled(Boolean.parseBoolean(musica));

            String pantallaCompleta = props.getProperty("pantalla_completa", "false");
            optionsPanel.setPantallaCompleta(Boolean.parseBoolean(pantallaCompleta));

            String volMusica = props.getProperty("volumen_musica", "80");
            String volSonidos = props.getProperty("volumen_sonidos", "80");
            optionsPanel.setVolumenMusica(Integer.parseInt(volMusica));
            optionsPanel.setVolumenSonidos(Integer.parseInt(volSonidos));

            BackgroundMusic.setVolume(Integer.parseInt(volMusica) / 100.0f);
            AudioManager.setVolumen(Integer.parseInt(volSonidos) / 100.0f);

        } catch (Exception e) {
            System.err.println("Error cargando preferencias: " + e.getMessage());
        }
    }

    public void flujoRegistroEInicioPartida() {
        if (jugadorActual == null) {
            int respuesta = JOptionPane.showConfirmDialog(view,
                    "No hay jugador seleccionado. ¿Desea ir a Perfiles para seleccionar uno?",
                    "Jugador no seleccionado",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (respuesta == JOptionPane.YES_OPTION) {
                view.mostrarPerfiles();
            }
            return;
        }

        arrancarPartidaConNuevoTablero();
    }

    private Jugador cargarJugadorPorNombre(String nombre) {
        try {
            List<Jugador> lista = TrabajarFichero.obtenerJugadores(datosFile);
            for (Jugador j : lista) {
                if (j.getNombre().equals(nombre)) return j;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean hayJugadoresEnDatos() {
        try {
            List<Jugador> lista = TrabajarFichero.obtenerJugadores(datosFile);
            return !lista.isEmpty();
        } catch (IllegalArgumentException iae) {
            return false;
        } catch (Exception e) {
            System.err.println("Advertencia comprobando jugadores en fichero: " + e.getMessage());
            return false;
        }
    }

    private void asegurarseDiccionarioCargado() throws Exception {
        if (dict != null) return;

        final JOptionPane pane = new JOptionPane("Cargando diccionario... espera",
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        final JDialog dialog = pane.createDialog(view, "Cargando");
        dialog.setModal(true);

        Exception[] loadEx = new Exception[1];

        Thread loader = new Thread(() -> {
            try {
                dict = new ArbolPrefijos();
                DictionaryLoader.loadIntoDictionary("game_files/diccionario.txt", dict);
            } catch (Exception ex) {
                loadEx[0] = ex;
            } finally {
                SwingUtilities.invokeLater(dialog::dispose);
            }
        }, "DictLoader");
        loader.start();
        dialog.setVisible(true);

        if (loadEx[0] != null) throw loadEx[0];
    }

    public void arrancarPartidaConNuevoTablero() {
        if (jugadorActual == null) return;
        try {
            asegurarseDiccionarioCargado();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "No se pudo cargar el diccionario:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BackgroundMusic.playSuspense();
        TableroBoggle board = new TableroBoggle();
        Partida partida = new Partida(jugadorActual.getNombre(), board, dict);
        view.mostrarJuego(partida);
    }

    public void mostrarJuego(Partida partida) {
        Icon reloj = null;
        GamePanel gamePanel = new GamePanel(partida, new GamePanel.GameActions() {
            @Override
            public void onTerminarPartida(int puntajeFinal) {
                try {
                    if (jugadorActual != null) {
                        jugadorActual.actualizarUltimaPartida(puntajeFinal);
                        TrabajarFichero.actualizarJugador(datosFile, jugadorActual);
                        JOptionPane.showMessageDialog(view,
                                "Puntuación de la partida guardada.\n" +
                                        "Jugador: " + jugadorActual.getNombre() + "\n" +
                                        "Puntos Totales: " + jugadorActual.getPuntos(),
                                "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(view,
                            "No se pudo guardar la partida:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    BackgroundMusic.playAmbiente();
                    view.volverMenu();
                }
            }

            @Override
            public void onVolverMenu() {
                BackgroundMusic.playAmbiente();
                view.volverMenu();
            }
        }, reloj);

        view.mostrarPanel(gamePanel, "game");
    }

    public void togglePantallaCompleta(boolean activar) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        boolean musicaEstabaSonando = BackgroundMusic.isPlaying();

        if (activar && device.isFullScreenSupported()) {
            Dimension normalSize = view.getSize();
            view.setFullScreenSize(normalSize);

            view.dispose();
            view.setUndecorated(true);
            device.setFullScreenWindow(view);
            view.setVisible(true);
            pantallaCompletaActiva = true;

        } else {
            device.setFullScreenWindow(null);
            view.dispose();
            view.setUndecorated(false);

            view.setSize(800, 700);
            view.setLocationRelativeTo(null);
            view.setVisible(true);
            pantallaCompletaActiva = false;
        }

        if (musicaEstabaSonando && BackgroundMusic.isEnabled()) {
            SwingUtilities.invokeLater(() -> {
                Timer timer = new Timer(100, e -> {
                    if (!BackgroundMusic.isPlaying()) {
                        BackgroundMusic.ensurePlaying();
                    }
                });
                timer.setRepeats(false);
                timer.start();
            });
        }

        guardarPreferencia("pantalla_completa", String.valueOf(activar));
    }

    private void guardarPreferencia(String clave, String valor) {
        try {
            Path prefsPath = Paths.get(resolveAppDataDirectory().getAbsolutePath(), "preferencias.properties");
            java.util.Properties props = new java.util.Properties();

            if (Files.exists(prefsPath)) {
                props.load(Files.newInputStream(prefsPath));
            }

            props.setProperty(clave, valor);
            props.store(Files.newOutputStream(prefsPath), "Preferencias de Boggle");
        } catch (Exception e) {
            System.err.println("Error guardando preferencia: " + e.getMessage());
        }
    }

    private void cargarPreferencias() {
        try {
            Path prefsPath = Paths.get(resolveAppDataDirectory().getAbsolutePath(), "preferencias.properties");
            if (!Files.exists(prefsPath)) return;

            java.util.Properties props = new java.util.Properties();
            props.load(Files.newInputStream(prefsPath));

            String pantallaCompleta = props.getProperty("pantalla_completa", "false");
            pantallaCompletaActiva = Boolean.parseBoolean(pantallaCompleta);

            if (pantallaCompletaActiva) {
                SwingUtilities.invokeLater(() -> {
                    togglePantallaCompleta(true);
                });
            }

        } catch (Exception e) {
            System.err.println("Error cargando preferencias: " + e.getMessage());
        }
    }

    public void guardarEstadoYSalir() {
        guardarPreferencia("pantalla_completa", String.valueOf(pantallaCompletaActiva));
        guardarPreferencia("audio_habilitado", String.valueOf(AudioManager.isEnabled()));
        guardarPreferencia("musica_habilitada", String.valueOf(BackgroundMusic.isEnabled()));
        guardarPreferencia("volumen_musica", String.valueOf((int)(BackgroundMusic.getVolume() * 100)));
        guardarPreferencia("volumen_sonidos", String.valueOf((int)(AudioManager.getVolumen() * 100)));

        guardarJugadorActual();
        BackgroundMusic.stop();
        System.exit(0);
    }

    public void iniciarTorneo() {
        try {
            List<Jugador> jugadoresExistentes = TrabajarFichero.obtenerJugadores(datosFile);
            if (jugadoresExistentes.size() < 2) {
                JOptionPane.showMessageDialog(view,
                        "Se necesitan al menos 2 jugadores registrados para un torneo.",
                        "Torneo no disponible", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JDialog torneoDialog = new JDialog(view, "Seleccionar Jugadores para el Torneo", true);
            torneoDialog.setLayout(new BorderLayout());
            torneoDialog.setSize(500, 400);
            torneoDialog.setLocationRelativeTo(view);

            JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
            panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel titulo = new JLabel("Seleccione jugadores para el torneo (2-4):");
            titulo.setFont(new Font("Arial", Font.BOLD, 14));
            panelPrincipal.add(titulo, BorderLayout.NORTH);

            JPanel panelJugadores = new JPanel();
            panelJugadores.setLayout(new BoxLayout(panelJugadores, BoxLayout.Y_AXIS));

            List<JCheckBox> checkboxes = new ArrayList<>();
            for (Jugador jugador : jugadoresExistentes) {
                JCheckBox checkBox = new JCheckBox(jugador.getNombre());
                checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
                checkBox.setBackground(Color.WHITE);
                panelJugadores.add(checkBox);
                checkboxes.add(checkBox);

            }

            JScrollPane scrollPane = new JScrollPane(panelJugadores);
            scrollPane.setPreferredSize(new Dimension(450, 250));
            panelPrincipal.add(scrollPane, BorderLayout.CENTER);

            JPanel panelBotones = new JPanel(new FlowLayout());
            JButton btnIniciar = new JButton("Iniciar Torneo");
            JButton btnCancelar = new JButton("Cancelar");

            btnIniciar.setBackground(new Color(70, 130, 180));
            btnIniciar.setForeground(Color.BLACK);
            btnCancelar.setBackground(new Color(200, 80, 80));
            btnCancelar.setForeground(Color.BLACK);

            panelBotones.add(btnIniciar);
            panelBotones.add(btnCancelar);
            panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

            torneoDialog.add(panelPrincipal);

            btnIniciar.addActionListener(e -> {
                int jugadoresSeleccionados = 0;
                List<Jugador> jugadoresParaTorneo = new ArrayList<>();

                for (int i = 0; i < checkboxes.size(); i++) {
                    if (checkboxes.get(i).isSelected()) {
                        jugadoresSeleccionados++;
                        jugadoresParaTorneo.add(jugadoresExistentes.get(i));
                    }
                }

                if (jugadoresSeleccionados < 2 || jugadoresSeleccionados > 4) {
                    JOptionPane.showMessageDialog(torneoDialog,
                            "Debe seleccionar entre 2 y 4 jugadores para el torneo.",
                            "Selección inválida", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    asegurarseDiccionarioCargado();
                    GestorTorneos gestor = new GestorTorneos();
                    String nombreTorneo = "Torneo " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
                    Torneo torneo = gestor.crearTorneo(nombreTorneo, dict);

                    for (Jugador jugador : jugadoresParaTorneo) {
                        torneo.agregarJugador(jugador.getNombre());
                    }

                    torneo.iniciar();
                    torneoDialog.dispose();
                    ejecutarTorneo(torneo, gestor, jugadoresParaTorneo);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(torneoDialog,
                            "Error al crear el torneo: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            btnCancelar.addActionListener(e -> {
                AudioManager.playClick();
                torneoDialog.dispose();
            });

            torneoDialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Error al iniciar torneo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarTorneo(Torneo torneo, GestorTorneos gestor, List<Jugador> jugadoresReales) {
        try {
            StringBuilder info = new StringBuilder();
            info.append("🏆 TORNEO INICIADO 🏆\n\n");
            info.append("Jugadores participantes:\n");
            for (JugadorTorneo jugador : torneo.getJugadores()) {
                info.append("• ").append(jugador.getNombre()).append("\n");
            }
            info.append("\nCada jugador tendrá un tablero diferente.\n");
            info.append("El orden de juego será aleatorio.");

            JOptionPane.showMessageDialog(view, info.toString(), "Torneo Iniciado", JOptionPane.INFORMATION_MESSAGE);

            java.util.Collections.shuffle(torneo.getPartidas());

            for (Partida partida : torneo.getPartidas()) {
                JugadorTorneo jugadorTorneo = torneo.getJugadores().stream()
                        .filter(j -> j.getNombre().equals(partida.getNombreJugador()))
                        .findFirst()
                        .orElse(null);

                if (jugadorTorneo != null) {
                    JOptionPane.showMessageDialog(view,
                            "Turno de: " + jugadorTorneo.getNombre(),
                            "Torneo en Progreso", JOptionPane.INFORMATION_MESSAGE);

                    mostrarPartidaTorneo(partida, jugadorTorneo);
                }
            }

            torneo.finalizar();
            String resultados = TorneoUtils.formatearResultados(torneo);
            JOptionPane.showMessageDialog(view, resultados, "🏆 Resultados del Torneo 🏆", JOptionPane.INFORMATION_MESSAGE);

            actualizarEstadisticasTorneo(torneo, jugadoresReales);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Error durante el torneo: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarPartidaTorneo(Partida partida, JugadorTorneo jugadorTorneo) {
        BackgroundMusic.playSuspense();

        JDialog partidaDialog = new JDialog(view, "Torneo - " + jugadorTorneo.getNombre(), true);
        partidaDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        partidaDialog.setSize(600, 700);
        partidaDialog.setLocationRelativeTo(view);

        GamePanel gamePanel = new GamePanel(partida, new GamePanel.GameActions() {
            @Override
            public void onTerminarPartida(int puntajeFinal) {
                jugadorTorneo.setPuntos(puntajeFinal);
                BackgroundMusic.playAmbiente();
                partidaDialog.dispose();
            }

            @Override
            public void onVolverMenu() {
                partida.finalizar();
                jugadorTorneo.setPuntos(partida.getPuntosTotales());
                BackgroundMusic.playAmbiente();
                partidaDialog.dispose();
            }
        }, null);

        partidaDialog.add(gamePanel);
        partidaDialog.setVisible(true);
    }

    private void actualizarEstadisticasTorneo(Torneo torneo, List<Jugador> jugadoresReales) {
        try {
            List<JugadorTorneo> ranking = torneo.obtenerRanking();

            for (JugadorTorneo jugadorTorneo : ranking) {
                for (Jugador jugadorReal : jugadoresReales) {
                    if (jugadorReal.getNombre().equals(jugadorTorneo.getNombre())) {
                        jugadorReal.actualizarDesdeTorneo(jugadorTorneo.getPuntos());
                        TrabajarFichero.actualizarJugador(datosFile, jugadorReal);
                        break;
                    }
                }
            }

            JOptionPane.showMessageDialog(view,
                    "Estadísticas del torneo guardadas exitosamente.",
                    "Torneo Finalizado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(view,
                    "Error al guardar estadísticas: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarJugadorGuardado() {
        try {
            Path prefsPath = Paths.get(resolveAppDataDirectory().getAbsolutePath(), "preferencias.properties");
            if (!Files.exists(prefsPath)) return;

            java.util.Properties props = new java.util.Properties();
            props.load(Files.newInputStream(prefsPath));

            String jugadorGuardado = props.getProperty(PREF_JUGADOR_ACTUAL, "false");
            if ("true".equals(jugadorGuardado)) {
                String nombre = props.getProperty(PREF_JUGADOR_NOMBRE, "");

                if (!nombre.isEmpty()) {
                    try {
                        Jugador jugadorReal = cargarJugadorPorNombre(nombre);
                        if (jugadorReal != null) {
                            this.jugadorActual = jugadorReal;
                            view.actualizarDisplayJugadorActual();
                            System.out.println("Jugador cargado: " + nombre);
                        } else {
                            System.out.println("Jugador no encontrado en archivo: " + nombre);
                            props.setProperty(PREF_JUGADOR_ACTUAL, "false");
                            props.remove(PREF_JUGADOR_NOMBRE);
                            props.remove(PREF_JUGADOR_PUNTOS);
                            props.remove(PREF_JUGADOR_PARTIDAS);
                            props.store(Files.newOutputStream(prefsPath), "Preferencias de Boggle");
                        }
                    } catch (OutOfMemoryError oome) {
                        System.err.println("ERROR: Archivo corrupto causando OutOfMemoryError");
                        props.setProperty(PREF_JUGADOR_ACTUAL, "false");
                        props.remove(PREF_JUGADOR_NOMBRE);
                        props.store(Files.newOutputStream(prefsPath), "Preferencias de Boggle");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error cargando jugador guardado: " + e.getMessage());
        }
    }

    private void guardarJugadorActual() {
        try {
            Path prefsPath = Paths.get(resolveAppDataDirectory().getAbsolutePath(), "preferencias.properties");
            java.util.Properties props = new java.util.Properties();

            if (Files.exists(prefsPath)) {
                props.load(Files.newInputStream(prefsPath));
            }

            if (jugadorActual != null) {
                props.setProperty(PREF_JUGADOR_ACTUAL, "true");
                props.setProperty(PREF_JUGADOR_NOMBRE, jugadorActual.getNombre());
                props.setProperty(PREF_JUGADOR_PUNTOS, String.valueOf(jugadorActual.getPuntos()));
                props.setProperty(PREF_JUGADOR_PARTIDAS, String.valueOf(jugadorActual.getPartidasJugadas()));
            } else {
                props.setProperty(PREF_JUGADOR_ACTUAL, "false");
                props.remove(PREF_JUGADOR_NOMBRE);
                props.remove(PREF_JUGADOR_PUNTOS);
                props.remove(PREF_JUGADOR_PARTIDAS);
            }

            props.store(Files.newOutputStream(prefsPath), "Preferencias de Boggle");

        } catch (Exception e) {
            System.err.println("Error guardando jugador actual: " + e.getMessage());
        }
    }

    // Getters y Setters
    public void setJugadorActual(Jugador jugador) {
        this.jugadorActual = jugador;
        view.actualizarDisplayJugadorActual();
        guardarJugadorActual();
    }

    public Jugador getJugadorActual() {
        return jugadorActual;
    }

    public File getDatosFile() {
        return datosFile;
    }

    public boolean isPantallaCompletaActiva() {
        return pantallaCompletaActiva;
    }

    public List<Jugador> obtenerTodosJugadores() {
        try {
            return TrabajarFichero.obtenerJugadores(datosFile);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

}