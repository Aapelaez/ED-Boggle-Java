package utils;

import logic.Jugador;
import logic.Partida;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class TrabajarFichero {
    public static void crearEncabezado(File fichero) throws FileNotFoundException {
        RandomAccessFile raf = abrirRaf(fichero);
        try {
            if (raf.length() == 0) {
                raf.writeInt(0); // Escribir el encabezado con el número de jugadores
                raf.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        public static long buscarJugadorFichero(File fichero, Jugador j) throws IOException, ClassNotFoundException {
            boolean encontrado = false;
            long posicion = -1;
            RandomAccessFile raf = abrirRaf(fichero);
            if(raf.length()>0) {
                raf.seek(0);
                int totalJugadores = raf.readInt();
                if (totalJugadores > 0) {
                    while (totalJugadores-- > 0 && !encontrado) {
                        long posicionActual = raf.getFilePointer();
                        byte[] array = new byte[raf.readInt()];
                        raf.read(array);
                        Jugador jugadorLeido = (Jugador) Convert.toObject(array);
                        if (jugadorLeido.compareTo(j)) {
                            encontrado = true;
                            posicion = posicionActual;
                        }
                    }
                }
                raf.close();
            }
            return posicion;
        }

        public static void actualizarJugador(File fichero, Jugador j) throws IOException, ClassNotFoundException {
        long posicion = buscarJugadorFichero(fichero, j);
        if (posicion != -1) {
            RandomAccessFile raf = abrirRaf(fichero);
            raf.seek(posicion);
            byte[] jugadorActualizadoBytes = Convert.toBytes(j);
            raf.writeInt(jugadorActualizadoBytes.length);
            raf.write(jugadorActualizadoBytes);
            raf.close();
        } else {
            throw new IllegalArgumentException("Jugador no encontrado en el fichero.");
        }

        }

        public static void agregarJugador(File fichero, Jugador j) throws IOException, ClassNotFoundException {
            if (buscarJugadorFichero(fichero, j) == -1) {
                RandomAccessFile raf = abrirRaf(fichero);
                raf.seek(0);
                int totalJugadores = raf.readInt();
                raf.seek(0);
                raf.writeInt(++totalJugadores);
                raf.seek(raf.length());
                byte[] jugadorBytes = Convert.toBytes(j);
                raf.writeInt(jugadorBytes.length);
                raf.write(jugadorBytes);
                raf.close();
            }
        }

        public static ArrayList<Jugador> obtenerJugadores(File fichero) throws IOException, ClassNotFoundException {
        RandomAccessFile raf = abrirRaf(fichero);
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



        private static RandomAccessFile abrirRaf(File fichero) throws FileNotFoundException {
            RandomAccessFile raf = new RandomAccessFile(fichero, "rw");
            return raf;
        }
    }
