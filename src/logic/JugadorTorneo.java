package logic;

import java.io.Serializable;

/**
 * Representa un jugador en un torneo
 */
public class JugadorTorneo implements Serializable {
    private final String nombre;
    private int puntos;
    private int posicion;

    private static final long serialVersionUID = 1L;

    public JugadorTorneo(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        this.nombre = nombre.trim();
        this.puntos = 0;
        this.posicion = 0;
    }

    // Getters y setters
    public String getNombre() { return nombre; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public int getPosicion() { return posicion; }
    public void setPosicion(int posicion) { this.posicion = posicion; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        JugadorTorneo that = (JugadorTorneo) obj;
        return nombre.equalsIgnoreCase(that.nombre);
    }

    @Override
    public int hashCode() {
        return nombre.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return String.format("JugadorTorneo{nombre='%s', puntos=%d, posicion=%d}",
                nombre, puntos, posicion);
    }
}