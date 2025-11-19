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
    private final File datosFile = new File("game_files/datos_partidas.dat");
    private Jugador jugadorActual;

    // Diccionario compartido (cargar una vez)
    private Dictionary dict;

    public MainFrame() {
        super("Boggle");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(520, 640));
        setLocationRelativeTo(null);

        inicializarFicheroDatos(); // asegura encabezado si está vacío
        initUI();

        setContentPane(root);
        cards.show(root, "menu");
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
            // Si tienes un logo, colócalo aquí:
            // labelLogo.setIcon(new ImageIcon("game_files/logo.png"));
            // labelLogo.setText("");
        });

        // Scoreboard (se pobla en mostrarPuntuaciones())
        scoreboardPanel = new ScoreboardPanel(() -> cards.show(root, "menu"));

        root.add(menuPanel, "menu");
        root.add(scoreboardPanel, "scores");
    }

    private void flujoRegistroEInicioPartida() {
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
        } else {
            // Usuario existente: pedir nombre y validar que esté
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
     * Carga el diccionario si aún no está cargado. La carga se realiza en un hilo
     * y se muestra un diálogo modal "Cargando diccionario..." mientras dura.
     */
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
        // Si tienes un GIF de reloj de arena, habilítalo:
        // reloj = new ImageIcon("game_files/hourglass.gif");

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