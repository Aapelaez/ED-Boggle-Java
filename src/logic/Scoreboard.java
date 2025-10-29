package logic;

import utils.Convert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

import utils.TrabajarFichero;

public class Scoreboard {

    /*Obtiene el ranking de los 10 mejores
    jugadores ordenados por puntos y en caso de empate por fecha de ultima partida*/
    public static ArrayList<Jugador> obtenerRanking(File f) throws IOException, ClassNotFoundException {
    ArrayList<Jugador> listado = TrabajarFichero.obtenerJugadores(f);
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
