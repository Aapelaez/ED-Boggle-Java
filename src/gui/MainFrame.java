package gui;

import logic.BoggleBoard;
import logic.Dictionary;
import logic.Jugador;
import logic.Partida;
import logic.TrieDictionary;
import logic.Scoreboard;
import utils.DictionaryLoader;
import utils.TrabajarFichero;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.io.IOException;
import java.nio.file.*;

/**
 * Ventana principal de la aplicación Swing.
 * - Maneja el flujo de registro/selección de usuario.
 * - Carga el diccionario (una sola vez).
 * - Crea Partida y la pasa al GamePanel.
 * - Muestra el Scoreboard.
 */
public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private MainMenuPanel menuPanel;
    private GamePanel gamePanel;
    private ScoreboardPanel scoreboardPanel;

    // Fichero de datos y jugador en uso
    private final File datosFile;
    private Jugador jugadorActual;

    // Diccionario compartido (cargar una vez)
    private Dictionary dict;

    public MainFrame() {
        super("Boggle");

        // Determina la carpeta de datos del usuario (userHome/.boggle)
        File appDataDir = resolveAppDataDirectory();
        // Aseguramos migración automática desde ubicaciones legacy (opcional)
        migrateLegacyGameFilesIfAny(appDataDir);

        this.datosFile = new File(appDataDir, "datos_partidas.dat");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 640));
        setLocationRelativeTo(null);

        inicializarFicheroDatos(); // asegura encabezado si está vacío
        initUI();

        setContentPane(root);
        cards.show(root, "menu");
    }

    /**
     * Resuelve la carpeta de datos del usuario: ~/.boggle (dentro de user.home).
     * Si no existe intenta crearla; si falla, falla-over a user.dir.
     */
    private File resolveAppDataDirectory() {
        // Soporta override por VM option para facilitar pruebas: -Dboggle.dataDir=...
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
                // fallback al directorio de trabajo actual
                return new File(System.getProperty("user.dir"));
            }
        }
        return dir;
    }

    /**
     * Busca ubicaciones antiguas donde la app pudo haber guardado "game_files" (junto al JAR o en working dir)
     * y copia su contenido a appDataDir si appDataDir está vacío. No sobrescribe si ya hay datos.
     */
    private void migrateLegacyGameFilesIfAny(File appDataDir) {
        // Si ya hay datos en appDataDir, no migramos
        File maybeDatos = new File(appDataDir, "datos_partidas.dat");
        if (maybeDatos.exists()) return;

        // Ubicaciones legacy a comprobar (carpeta del JAR y directorio de trabajo)
        try {
            // carpeta del JAR / clases
            String codeLocation = MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            File codeFile = new File(codeLocation);
            File jarParent = codeFile.isFile() ? codeFile.getParentFile() : codeFile;
            File legacy1 = new File(jarParent, "game_files");

            // directorio de trabajo actual
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
            // ignore and continue — migración es opcional
        }
    }

    /**
     * Copia recursivamente archivos desde srcDir (game_files) dentro de destDir solo si no existen.
     * Solo copia archivos top-level esperados como datos y recursos; evita sobrescribir.
     */
    private void copyDirectoryIfNotExists(Path srcDir, Path destDir) {
        try {
            if (!Files.exists(destDir)) Files.createDirectories(destDir);
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(srcDir)) {
                for (Path entry : ds) {
                    Path dest = destDir.resolve(entry.getFileName());
                    if (Files.exists(dest)) continue; // no sobrescribir
                    Files.copy(entry, dest, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        } catch (IOException ignored) {
            // migración no crítica; no interrumpir inicio
        }
    }

    private void inicializarFicheroDatos() {
        try {
            // Crear carpeta si no existe
            File parent = datosFile.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();

            // Crear el archivo si no existe
            if (!datosFile.exists()) datosFile.createNewFile();

            // Escribir encabezado solo si el fichero está vacío
            TrabajarFichero.crearEncabezado(datosFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo inicializar el fichero de datos:\n" + e.getMessage(),
                    "Error de inicialización", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initUI() {
        // Menú principal
        menuPanel = new MainMenuPanel(new MainMenuPanel.MenuActions() {
            @Override
            public void onIniciarPartida() {
                AudioManager.playClick();
                flujoRegistroEInicioPartida();
            }

            @Override
            public void onVerPuntuaciones() {
                AudioManager.playClick();
                mostrarPuntuaciones();
            }

            @Override
            public void onSalir() {
                AudioManager.playClick();
                System.exit(0);
            }

            @Override
            public void onToggleAudio(boolean enabled) {
                AudioManager.setEnabled(enabled);
            }
        }, labelLogo -> {
            // Si tienes un logo en disco (no dentro del JAR), podrías cargarlo así:
            // File logoFile = new File(resolveAppDataDirectory(), "logo.png");
            // if (logoFile.exists()) labelLogo.setIcon(new ImageIcon(logoFile.getAbsolutePath()));
        });

        // Scoreboard (se pobla en mostrarPuntuaciones())
        scoreboardPanel = new ScoreboardPanel(() -> cards.show(root, "menu"));

        root.add(menuPanel, "menu");
        root.add(scoreboardPanel, "scores");
    }

    /**
     * Nuevo comportamiento: si no hay jugadores en el fichero (encabezado == 0),
     * saltamos la pregunta "¿Eres usuario nuevo?" y solicitamos directamente el nombre
     * para crear el primer usuario.
     */
    private void flujoRegistroEInicioPartida() {
        // Si el fichero no tiene jugadores, no preguntamos; vamos directo al registro nuevo.
        if (!hayJugadoresEnDatos()) {
            JOptionPane.showMessageDialog(this,
                    "No hay jugadores registrados. Se solicitará crear un nuevo usuario.",
                    "Registro", JOptionPane.INFORMATION_MESSAGE);
            registrarUsuarioNuevoYEmpezar();
            return;
        }

        int resp = JOptionPane.showConfirmDialog(
                this,
                "¿Eres usuario nuevo?",
                "Registro",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resp == JOptionPane.CLOSED_OPTION) return;

        if (resp == JOptionPane.YES_OPTION) {
            // Usuario nuevo: validar que no exista y agregar
            registrarUsuarioNuevoYEmpezar();
        } else {
            // Usuario existente: pedir nombre y validar que esté
            registrarUsuarioExistenteYEmpezar();
        }
    }

    // Extraigo el flujo para nuevo usuario en un método para mantener el código claro
    private void registrarUsuarioNuevoYEmpezar() {
        while (true) {
            String nombre = solicitarNombre("Introduce un nombre de usuario (nuevo):");
            if (nombre == null) return; // canceló
            try {
                Jugador candidato = new Jugador(nombre);
                long pos = TrabajarFichero.buscarJugadorFichero(datosFile, candidato);
                if (pos != -1) {
                    JOptionPane.showMessageDialog(this,
                            "Ya existe un usuario con ese nombre. Elige otro.",
                            "Nombre repetido", JOptionPane.WARNING_MESSAGE);
                    continue; // pedir de nuevo
                }
                // No existe: agregar al final
                TrabajarFichero.agregarJugador(datosFile, candidato);
                jugadorActual = candidato;
                arrancarPartidaConNuevoTablero();
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error gestionando el registro:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private void registrarUsuarioExistenteYEmpezar() {
        while (true) {
            String nombre = solicitarNombre("Introduce tu nombre de usuario (existente):");
            if (nombre == null) return; // canceló
            try {
                Jugador probe = new Jugador(nombre);
                long pos = TrabajarFichero.buscarJugadorFichero(datosFile, probe);
                if (pos == -1) {
                    int retry = JOptionPane.showConfirmDialog(this,
                            "No se encontró ese usuario. ¿Intentar de nuevo?",
                            "Usuario no encontrado", JOptionPane.YES_NO_OPTION);
                    if (retry == JOptionPane.YES_OPTION) {
                        continue;
                    } else {
                        return;
                    }
                }
                // Cargar el jugador real desde fichero y usarlo
                jugadorActual = cargarJugadorPorNombre(nombre);
                if (jugadorActual == null) {
                    JOptionPane.showMessageDialog(this,
                            "No se pudo cargar el usuario. Inténtalo de nuevo.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    continue;
                }
                arrancarPartidaConNuevoTablero();
                break;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error buscando el usuario:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private String solicitarNombre(String mensaje) {
        while (true) {
            String nombre = JOptionPane.showInputDialog(this, mensaje, "Usuario", JOptionPane.QUESTION_MESSAGE);
            if (nombre == null) return null; // cancelado
            nombre = nombre.trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El nombre no puede estar vacío.", "Validación", JOptionPane.WARNING_MESSAGE);
                continue;
            }
            return nombre;
        }
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

    /**
     * Comprueba si hay jugadores en el fichero delegando en TrabajarFichero.obtenerJugadores(...).
     * Antes se leía directamente el encabezado; ahora delegamos en la clase util para mantener la
     * responsabilidad de acceso al fichero centralizada.
     */
    private boolean hayJugadoresEnDatos() {
        try {
            List<Jugador> lista = TrabajarFichero.obtenerJugadores(datosFile);
            return lista != null && !lista.isEmpty();
        } catch (IllegalArgumentException iae) {
            // obtenerJugadores lanza IllegalArgumentException cuando no hay jugadores -> lo interpretamos como vacío
            return false;
        } catch (IOException | ClassNotFoundException e) {
            // Cualquier otro error lo interpretamos como "no hay jugadores" y registramos por si se necesita depurar.
            System.err.println("Advertencia comprobando jugadores en fichero: " + e.getMessage());
            return false;
        } catch (Exception e) {
            // Cualquier excepción inesperada -> asumimos no hay jugadores
            System.err.println("Advertencia inesperada comprobando jugadores en fichero: " + e.getMessage());
            return false;
        }
    }

    private void asegurarseDiccionarioCargado() throws Exception {
        if (dict != null) return;
        // Mensaje simple mientras carga (la carga puede tardar)
        final JOptionPane pane = new JOptionPane("Cargando diccionario... espera", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);
        final JDialog dialog = pane.createDialog(this, "Cargando");
        dialog.setModal(true);

        Exception loadEx[] = new Exception[1];

        Thread loader = new Thread(() -> {
            try {
                dict = new TrieDictionary();
                // DictionaryLoader carga desde classpath; el diccionario puede permanecer dentro del JAR
                DictionaryLoader.loadIntoDictionary("game_files/diccionario.txt", dict);
            } catch (Exception ex) {
                loadEx[0] = ex;
            } finally {
                // close dialog on EDT
                SwingUtilities.invokeLater(dialog::dispose);
            }
        }, "DictLoader");
        loader.start();
        dialog.setVisible(true); // bloquea hasta que loader llama a dispose()

        if (loadEx[0] != null) throw loadEx[0];
    }

    private void arrancarPartidaConNuevoTablero() {
        if (jugadorActual == null) return;
        try {
            asegurarseDiccionarioCargado();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el diccionario:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BoggleBoard board = new BoggleBoard();
        Partida partida = new Partida(jugadorActual.getNombre(), board, dict);
        mostrarJuego(partida);
    }

    private void mostrarJuego(Partida partida) {
        Icon reloj = null;

        gamePanel = new GamePanel(partida, new GamePanel.GameActions() {
            @Override
            public void onTerminarPartida(int puntajeFinal) {
                // ACTUALIZAR Y GUARDAR EL JUGADOR usando sólo los puntos de LA PARTIDA
                try {
                    if (jugadorActual != null) {
                        jugadorActual.actualizarUltimaPartida(puntajeFinal); // actualiza puntos acumulados y fecha
                        // Persistir cambios
                        TrabajarFichero.actualizarJugador(datosFile, jugadorActual);
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Puntuación de la partida guardada.\n" +
                                        "Jugador: " + jugadorActual.getNombre() + "\n" +
                                        "Puntos Totales: " + jugadorActual.getPuntos(),
                                "Fin de partida", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(MainFrame.this,
                            "No se pudo guardar la partida:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    cards.show(root, "menu");
                }
            }

            @Override
            public void onVolverMenu() {
                cards.show(root, "menu");
            }
        }, reloj);

        root.add(gamePanel, "game");
        cards.show(root, "game");
    }

    private void mostrarPuntuaciones() {
        try {
            // Carga real del top 10 desde tu lógica
            java.util.ArrayList<Jugador> top = Scoreboard.obtenerRanking(datosFile);
            scoreboardPanel.setJugadores(top);
        } catch (Exception e) {
            // Manejo del caso de fichero sin jugadores o cualquier otro error
            scoreboardPanel.setMensaje("No hay jugadores registrados todavía.");
        }
        cards.show(root, "scores");
    }
}