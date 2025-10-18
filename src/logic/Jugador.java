package logic;

import java.util.Date;

public class Jugador implements java.io.Serializable {
    private String nombre;
    private int puntos;
    private Date ultimaPartida;
    private int partidasJugadas;
    private static final long serialVersionUID = 1L;


    public Jugador(String nombre) {
        setNombre(nombre);
        setPuntos(0);
        setPartidasJugadas(0);
    }

    private void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntos() {
        return puntos;
    }

    private void setPartidasJugadas(int partidasJugadas) {
        this.partidasJugadas = partidasJugadas;
    }
    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    private void setPuntos(int puntos) {
        this.puntos = puntos;
    }

    private void actualizarPuntos(int puntos){
        setPuntos(getPuntos()+puntos);
    }
    public Date getUltimaPartida() {
        return ultimaPartida;
    }
    public void actualizarUltimaPartida(int puntos) {
        this.ultimaPartida = new Date();
        actualizarPuntos(puntos);
        actualizarPartidasJugadas();
    }

    private void actualizarPartidasJugadas() {
        this.partidasJugadas++;
    }

    public boolean compareTo(Jugador j) {
        return this.getNombre().equals(j.getNombre());
    }

}
