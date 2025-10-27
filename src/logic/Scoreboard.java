package logic;

import utils.Convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Scoreboard {
private final RandomAccessFile raf;

public Scoreboard (File f) throws FileNotFoundException {
    if(f!=null) {
        this.raf = new RandomAccessFile(f, "r");
    }else {
        throw new NullPointerException("Archivo nulo");
    }
}

public ArrayList<Jugador> obtenerJugadores() throws IOException, ClassNotFoundException {
    ArrayList<Jugador> jugadores = new ArrayList<>();
    if (raf.length() > 0) {
        raf.seek(0);
        int totalJugadores = raf.readInt();
        if (totalJugadores > 0) {
            while (totalJugadores-- > 0) {
                byte[] array = new byte[raf.readInt()];
                raf.read(array);
                Jugador j= (Jugador)Convert.toObject(array);
                jugadores.add(j);
            }
            raf.close();
            }else {
            throw new IllegalArgumentException("No hay jugadores en el archivo");
        }
    }else {
        throw new IllegalArgumentException("No hay datos en el archivo");
    }
    return jugadores;
}

    /*Obtiene el ranking de los 10 mejores
    jugadores ordenados por puntos y en caso de empate por fecha de ultima partida*/
    public ArrayList<Jugador> obtenerRanking() throws IOException, ClassNotFoundException {
    ArrayList<Jugador> listado = obtenerJugadores();
        listado.sort(Comparator
                .comparingInt(Jugador::getPuntos).reversed()
                .thenComparing((Jugador j) -> {
                    Date d = j.getUltimaPartida();
                    return d == null ? Long.MIN_VALUE : d.getTime();
                }, Comparator.reverseOrder())
        );
        if (listado.size() > 10) {
            listado=new ArrayList<>(listado.subList(0, 10));
        }

        return listado;
    }

}
