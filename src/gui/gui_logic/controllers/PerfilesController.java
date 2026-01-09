package gui.gui_logic.controllers;

import sound.AudioManager;
import gui.frames.MainFrame;
import gui.panels.PerfilesPanel;
import logic.Jugador;
import utils.TrabajarFichero;

import javax.swing.*;
import java.io.File;
import java.util.List;

/**
 * Controlador para PerfilesPanel - Maneja la lógica de gestión de perfiles
 */
public class PerfilesController {

    private final PerfilesPanel view;
    private final File datosFile;
    private final MainFrame parentFrame;

    public PerfilesController(PerfilesPanel view, File datosFile, MainFrame parentFrame) {
        this.view = view;
        this.datosFile = datosFile;
        this.parentFrame = parentFrame;
    }

    public void cargarJugadores() {
        try {
            System.out.println("=== CARGANDO JUGADORES ===");
            System.out.println("Archivo: " + datosFile.getAbsolutePath());
            System.out.println("Existe: " + datosFile.exists());
            System.out.println("Tamaño: " + datosFile.length() + " bytes");

            List<Jugador> jugadores = TrabajarFichero.obtenerJugadores(datosFile);
            System.out.println("Jugadores leídos: " + jugadores.size());

            DefaultListModel<PerfilesPanel.JugadorItem> modelo = view.getModeloJugadores();
            modelo.clear();

            for (Jugador jugador : jugadores) {
                System.out.println("  - " + jugador.getNombre());
                modelo.addElement(view.new JugadorItem(jugador));
            }

            if (modelo.isEmpty()) {
                System.out.println("Lista vacía - mostrando lista vacía");
                view.setListaJugadoresEnabled(false);
            } else {
                view.setListaJugadoresEnabled(true);
                System.out.println("Lista cargada exitosamente");
            }

            System.out.println("=== FIN CARGA ===");
        } catch (IllegalArgumentException iae) {
            System.out.println("Caso especial: " + iae.getMessage());
            view.setListaJugadoresEnabled(false);
        } catch (Exception e) {
            System.err.println("ERROR en cargarJugadores: " + e.getMessage());
            e.printStackTrace();

            Jugador dummy = new Jugador("Error cargando jugadores: " + e.getMessage());
            PerfilesPanel.JugadorItem item = view.new JugadorItem(dummy);
            view.getModeloJugadores().addElement(item);
            view.setListaJugadoresEnabled(false);
        }
    }

    public void seleccionarJugador(Jugador jugador) {
        if (jugador.getNombre().contains("Error cargando")) {
            return;
        }

        AudioManager.playClick();
        parentFrame.setJugadorActual(jugador);
        view.getActions().onVolver();
    }

    public void agregarJugador() {
        AudioManager.playClick();

        while (true) {
            String nombre = JOptionPane.showInputDialog(view, "Ingrese el nombre del nuevo jugador:",
                    "Agregar Jugador", JOptionPane.QUESTION_MESSAGE);

            if (nombre == null) {
                return;
            }

            nombre = nombre.trim();
            if (nombre.isEmpty()) {
                JOptionPane.showMessageDialog(view, "El nombre no puede estar vacío.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                continue;
            }

            try {
                Jugador nuevoJugador = new Jugador(nombre);
                TrabajarFichero.agregarJugador(datosFile, nuevoJugador);
                cargarJugadores();

                int respuesta = JOptionPane.showConfirmDialog(view,
                        "¿Desea seleccionar a '" + nombre + "' como jugador actual?",
                        "Seleccionar jugador",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (respuesta == JOptionPane.YES_OPTION) {
                    parentFrame.setJugadorActual(nuevoJugador);
                }

                break;

            } catch (Exception e) {
                JOptionPane.showMessageDialog(view, "Error al agregar jugador: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void editarJugador(int indice) {
        AudioManager.playClick();
        if (indice == -1) {
            JOptionPane.showMessageDialog(view, "Seleccione un jugador para editar",
                    "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            PerfilesPanel.JugadorItem item = view.getModeloJugadores().getElementAt(indice);
            Jugador jugadorOriginal = item.getJugador();

            String nuevoNombre = JOptionPane.showInputDialog(view, "Nuevo nombre para " +
                    jugadorOriginal.getNombre() + ":", "Editar Jugador", JOptionPane.QUESTION_MESSAGE);

            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                nuevoNombre = nuevoNombre.trim();

                if (nuevoNombre.equals(jugadorOriginal.getNombre())) {
                    JOptionPane.showMessageDialog(view, "El nombre no ha cambiado",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                Jugador jugadorPrueba = new Jugador(nuevoNombre);
                if (TrabajarFichero.buscarJugadorFichero(datosFile, jugadorPrueba) != -1) {
                    JOptionPane.showMessageDialog(view, "Ya existe un jugador con ese nombre",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Jugador jugadorActualizado = new Jugador(nuevoNombre);

                TrabajarFichero.eliminarJugador(datosFile, jugadorOriginal);
                TrabajarFichero.agregarJugador(datosFile, jugadorActualizado);

                cargarJugadores();
                JOptionPane.showMessageDialog(view, "Jugador actualizado exitosamente",
                        "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error al editar jugador: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void eliminarJugadorConfirmacion(Jugador jugador) {
        if (jugador.getNombre().contains("Error cargando")) {
            return;
        }

        AudioManager.playClick();
        int confirmacion = JOptionPane.showConfirmDialog(view,
                "¿Está seguro de eliminar al jugador: " + jugador.getNombre() + "?\n" +
                        "Esta acción no se puede deshacer.",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                if (parentFrame.getJugadorActual() != null &&
                        parentFrame.getJugadorActual().getNombre().equals(jugador.getNombre())) {
                    parentFrame.setJugadorActual(null);
                }

                TrabajarFichero.eliminarJugador(datosFile, jugador);
                cargarJugadores();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(view,
                        "Error al eliminar jugador: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}