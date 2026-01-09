package logic;

import java.util.List;

/**
 * Utilidades para manejar torneos en la interfaz de usuario
 */
public class TorneoUtils {

    /**
     * Valida si un nombre de jugador puede ser agregado al torneo
     */
    public static String validarJugadorTorneo(Torneo torneo, String nombreJugador) {
        if (torneo == null) {
            return "No hay torneo activo";
        }
        if (torneo.isIniciado()) {
            return "El torneo ya ha comenzado, no se pueden agregar más jugadores";
        }
        if (nombreJugador == null || nombreJugador.trim().isEmpty()) {
            return "El nombre del jugador no puede estar vacío";
        }
        if (torneo.getCantidadJugadores() >= Torneo.MAX_JUGADORES) {
            return "El torneo ya tiene el máximo de " + Torneo.MAX_JUGADORES + " jugadores";
        }

        // Verificar duplicados
        for (JugadorTorneo j : torneo.getJugadores()) {
            if (j.getNombre().equalsIgnoreCase(nombreJugador.trim())) {
                return "Ya existe un jugador con ese nombre en el torneo";
            }
        }

        return null; // Válido
    }

    /**
     * Formatea los resultados del torneo para mostrar
     */
    public static String formatearResultados(Torneo torneo) {
        if (!torneo.isFinalizado()) {
            return "El torneo aún no ha finalizado";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🏆 RESULTADOS DEL TORNEO 🏆\n\n");
        sb.append("Nombre: ").append(torneo.getNombre()).append("\n\n");

        List<JugadorTorneo> ranking = torneo.obtenerRanking();
        List<JugadorTorneo> ganadores = torneo.obtenerGanadores();

        if (ganadores.size() == 1) {
            sb.append("🥇 GANADOR: ").append(ganadores.get(0).getNombre())
                    .append(" (").append(ganadores.get(0).getPuntos()).append(" puntos)\n\n");
        } else {
            sb.append("🥇 EMPATE ENTRE: ");
            for (int i = 0; i < ganadores.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(ganadores.get(i).getNombre());
            }
            sb.append(" (").append(ganadores.get(0).getPuntos()).append(" puntos)\n\n");
        }

        sb.append("RANKING FINAL:\n");
        for (int i = 0; i < ranking.size(); i++) {
            JugadorTorneo jugador = ranking.get(i);
            String medalla = "";
            if (i == 0) medalla = "🥇 ";
            else if (i == 1) medalla = "🥈 ";
            else if (i == 2) medalla = "🥉 ";

            sb.append(medalla).append(jugador.getNombre())
                    .append(" - ").append(jugador.getPuntos()).append(" puntos\n");
        }

        return sb.toString();
    }
}