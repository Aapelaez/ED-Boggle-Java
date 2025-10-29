package logic;

import cu.edu.cujae.ceis.graph.LinkedGraph;
import utils.TextNormalizer;

import java.util.*;

/**
 * Representa una partida de Boggle.
 *
 * Responsabilidades:
 *  - Mantener el tablero (BoggleBoard) y su grafo asociado.
 *  - Validar palabras del usuario.
 *  - Llevar las palabras aceptadas (sin duplicados).
 *  - Calcular y acumular la puntuación.
 */
public class Partida {

    public enum EstadoValidacionPalabra {
        OK,
        MUY_CORTA,
        CARACTERES_INVALIDOS,
        NO_FORMABLE_EN_TABLERO,
        NO_EN_DICCIONARIO,
        REPETIDA,
        ERROR
    }

    public static class ResultadoEnvio {
        public final EstadoValidacionPalabra estado;
        public final String normalizada;
        public final int puntosGanados;

        public ResultadoEnvio(EstadoValidacionPalabra estado, String normalizada, int puntosGanados) {
            this.estado = estado;
            this.normalizada = normalizada;
            this.puntosGanados = puntosGanados;
        }
    }

    private final String nombreJugador;
    private final BoggleBoard tablero;
    private final LinkedGraph grafo;
    private final BoggleSolver solver;
    private final Dictionary diccionario;
    private final GameWordValidator validador;
    private final Set<String> palabrasAceptadas = new LinkedHashSet<>();
    private int puntosTotales = 0;
    private long inicioMillis = 0;
    private long finMillis = 0;
    private boolean iniciada = false;
    private boolean finalizada = false;

    public Partida(String nombreJugador, BoggleBoard tablero, Dictionary diccionario) {
        if (nombreJugador == null || nombreJugador.trim().isEmpty()) throw new IllegalArgumentException("Nombre de jugador inválido");
        if (diccionario == null) throw new IllegalArgumentException("El diccionario no puede ser null");

        this.nombreJugador = nombreJugador.trim();
        this.tablero = tablero != null ? tablero : new BoggleBoard();
        this.diccionario = diccionario;

        this.grafo = BoggleGraphBuilder.build(this.tablero);
        this.solver = new BoggleSolver(this.grafo);
        this.validador = new GameWordValidator(this.solver, this.diccionario);
    }

    public void iniciar() {
        if (!iniciada) {
            this.inicioMillis = System.currentTimeMillis();
            this.iniciada = true;
        }
    }

    /**
     * Intenta aceptar una palabra introducida por el usuario.
     * Si la partida está finalizada, lanza excepción.
     */
    public ResultadoEnvio enviarPalabra(String palabraIngresada) {
        if (!iniciada) iniciar();
        if (finalizada) {
            throw new IllegalStateException("La partida ya ha finalizado");
        }
        if (palabraIngresada == null) return new ResultadoEnvio(EstadoValidacionPalabra.CARACTERES_INVALIDOS, "", 0);

        GameWordValidator.Validation v = validador.validateUserWord(palabraIngresada);

        switch (v.result) {
            case TOO_SHORT:
                return new ResultadoEnvio(EstadoValidacionPalabra.MUY_CORTA, v.normalized, 0);
            case INVALID_CHARACTERS:
                return new ResultadoEnvio(EstadoValidacionPalabra.CARACTERES_INVALIDOS, v.normalized, 0);
            case NOT_FORMABLE_ON_BOARD:
                return new ResultadoEnvio(EstadoValidacionPalabra.NO_FORMABLE_EN_TABLERO, v.normalized, 0);
            case NOT_IN_DICTIONARY:
                return new ResultadoEnvio(EstadoValidacionPalabra.NO_EN_DICCIONARIO, v.normalized, 0);
            case OK:
                String normalizada = v.normalized;
                if (palabrasAceptadas.contains(normalizada)) {
                    return new ResultadoEnvio(EstadoValidacionPalabra.REPETIDA, normalizada, 0);
                }
                int puntos = puntuarPalabra(normalizada);
                palabrasAceptadas.add(normalizada);
                puntosTotales += puntos;
                return new ResultadoEnvio(EstadoValidacionPalabra.OK, normalizada, puntos);
            default:
                return new ResultadoEnvio(EstadoValidacionPalabra.ERROR, v.normalized, 0);
        }
    }

    /**
     * Calcula la puntuación según la longitud de la palabra, usando switch-case y una sola variable.
     */
    private int puntuarPalabra(String palabra) {
        int puntos = 0;
        if (palabra != null) {
            int longitud = palabra.length();
            switch (longitud) {
                case 0:
                case 1:
                case 2:
                    puntos = 0;
                    break;
                case 3:
                case 4:
                    puntos = 1;
                    break;
                case 5:
                    puntos = 2;
                    break;
                case 6:
                    puntos = 3;
                    break;
                case 7:
                    puntos = 5;
                    break;
                default:
                    if (longitud >= 8) puntos = 11;
            }
        }
        return puntos;
    }

    public void finalizar() {
        if (!iniciada) iniciar();
        if (!finalizada) {
            this.finMillis = System.currentTimeMillis();
            this.finalizada = true;
        }
    }

    public int getPuntosTotales() {
        return puntosTotales;
    }

    public List<String> getPalabrasAceptadas() {
        return new ArrayList<>(palabrasAceptadas);
    }

    public BoggleBoard getTablero() {
        return tablero;
    }

    public String getNombreJugador() {
        return nombreJugador;
    }

    public boolean estaFinalizada() {
        return finalizada;
    }
}