package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un torneo de Boggle con múltiples jugadores (2-4)
 */
public class Torneo {
    public static final int MAX_JUGADORES = 4;
    public static final int MIN_JUGADORES = 2;

    private final String nombre;
    private final List<JugadorTorneo> jugadores;
    private final List<Partida> partidas;
    private final Dictionary diccionario;
    private boolean iniciado = false;
    private boolean finalizado = false;

    public Torneo(String nombre, Dictionary diccionario) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del torneo no puede estar vacío");
        }
        if (diccionario == null) {
            throw new IllegalArgumentException("El diccionario no puede ser null");
        }

        this.nombre = nombre.trim();
        this.diccionario = diccionario;
        this.jugadores = new ArrayList<>();
        this.partidas = new ArrayList<>();
    }

    /**
     * Agrega un jugador al torneo
     */
    public boolean agregarJugador(String nombreJugador) {
        if (iniciado || finalizado) {
            throw new IllegalStateException("No se pueden agregar jugadores después de iniciar el torneo");
        }
        if (jugadores.size() >= MAX_JUGADORES) {
            return false;
        }

        // Verificar que el jugador no esté duplicado
        for (JugadorTorneo j : jugadores) {
            if (j.getNombre().equalsIgnoreCase(nombreJugador)) {
                return false;
            }
        }

        jugadores.add(new JugadorTorneo(nombreJugador));
        return true;
    }

    /**
     * Inicia el torneo creando partidas para todos los jugadores CON TABLEROS DIFERENTES
     */
    public void iniciar() {
        if (iniciado) {
            throw new IllegalStateException("El torneo ya ha sido iniciado");
        }
        if (jugadores.size() < MIN_JUGADORES) {
            throw new IllegalStateException("Se necesitan al menos " + MIN_JUGADORES + " jugadores para iniciar el torneo");
        }

        // Crear un tablero DIFERENTE para cada jugador
        for (JugadorTorneo jugador : jugadores) {
            BoggleBoard tablero = new BoggleBoard(); // Cada jugador tiene su propio tablero
            Partida partida = new Partida(jugador.getNombre(), tablero, diccionario);
            partidas.add(partida);
            partida.iniciar();
        }

        iniciado = true;
    }

    /**
     * Envía una palabra para un jugador específico
     */
    public Partida.ResultadoEnvio enviarPalabra(String nombreJugador, String palabra) {
        if (!iniciado || finalizado) {
            throw new IllegalStateException("El torneo no está en curso");
        }

        Partida partida = buscarPartidaJugador(nombreJugador);
        if (partida == null) {
            throw new IllegalArgumentException("Jugador no encontrado en el torneo: " + nombreJugador);
        }

        return partida.enviarPalabra(palabra);
    }

    /**
     * Finaliza el torneo y calcula los resultados
     */
    public void finalizar() {
        if (!iniciado || finalizado) {
            throw new IllegalStateException("El torneo no está en curso o ya finalizó");
        }

        for (Partida partida : partidas) {
            if (!partida.estaFinalizada()) {
                partida.finalizar();
            }
        }

        // Actualizar puntuaciones de los jugadores
        for (int i = 0; i < jugadores.size(); i++) {
            JugadorTorneo jugador = jugadores.get(i);
            Partida partida = partidas.get(i);
            jugador.setPuntos(partida.getPuntosTotales());
        }

        // Ordenar jugadores por puntuación (mayor a menor)
        jugadores.sort((j1, j2) -> Integer.compare(j2.getPuntos(), j1.getPuntos()));

        // Asignar posiciones
        for (int i = 0; i < jugadores.size(); i++) {
            jugadores.get(i).setPosicion(i + 1);
        }

        finalizado = true;
    }

    /**
     * Obtiene el ranking del torneo
     */
    public List<JugadorTorneo> obtenerRanking() {
        if (!finalizado) {
            throw new IllegalStateException("El torneo debe finalizar primero para obtener el ranking");
        }
        return new ArrayList<>(jugadores);
    }

    /**
     * Obtiene el ganador del torneo (puede haber empates)
     */
    public List<JugadorTorneo> obtenerGanadores() {
        if (!finalizado) {
            throw new IllegalStateException("El torneo debe finalizar primero para determinar ganadores");
        }

        List<JugadorTorneo> ganadores = new ArrayList<>();
        if (jugadores.isEmpty()) return ganadores;

        int maxPuntos = jugadores.get(0).getPuntos();
        for (JugadorTorneo jugador : jugadores) {
            if (jugador.getPuntos() == maxPuntos) {
                ganadores.add(jugador);
            } else {
                break;
            }
        }

        return ganadores;
    }

    // Getters
    public String getNombre() { return nombre; }
    public List<JugadorTorneo> getJugadores() { return new ArrayList<>(jugadores); }
    public List<Partida> getPartidas() { return new ArrayList<>(partidas); }
    public boolean isIniciado() { return iniciado; }
    public boolean isFinalizado() { return finalizado; }
    public int getCantidadJugadores() { return jugadores.size(); }

    private Partida buscarPartidaJugador(String nombreJugador) {
        for (Partida partida : partidas) {
            if (partida.getNombreJugador().equalsIgnoreCase(nombreJugador)) {
                return partida;
            }
        }
        return null;
    }
}