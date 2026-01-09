package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona múltiples torneos
 */
public class GestorTorneos {
    private final List<Torneo> torneos;
    private Torneo torneoActual;

    public GestorTorneos() {
        this.torneos = new ArrayList<>();
    }

    /**
     * Crea un nuevo torneo
     */
    public Torneo crearTorneo(String nombre, Diccionario diccionario) {
        Torneo torneo = new Torneo(nombre, diccionario);
        torneos.add(torneo);
        torneoActual = torneo;
        return torneo;
    }

    /**
     * Agrega un jugador al torneo actual
     */
    public boolean agregarJugadorATorneoActual(String nombreJugador) {
        if (torneoActual == null) {
            throw new IllegalStateException("No hay ningún torneo activo");
        }
        return torneoActual.agregarJugador(nombreJugador);
    }

    /**
     * Inicia el torneo actual
     */
    public void iniciarTorneoActual() {
        if (torneoActual == null) {
            throw new IllegalStateException("No hay ningún torneo activo");
        }
        torneoActual.iniciar();
    }

    /**
     * Finaliza el torneo actual
     */
    public void finalizarTorneoActual() {
        if (torneoActual == null) {
            throw new IllegalStateException("No hay ningún torneo activo");
        }
        torneoActual.finalizar();
    }

    // Getters
    public Torneo getTorneoActual() { return torneoActual; }
    public List<Torneo> getTorneos() { return new ArrayList<>(torneos); }

    /**
     * Verifica si hay un torneo en curso
     */
    public boolean hayTorneoEnCurso() {
        return torneoActual != null && torneoActual.isIniciado() && !torneoActual.isFinalizado();
    }
}