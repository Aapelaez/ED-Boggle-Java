package utils;

import logic.Jugador;
import logic.Partida;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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


        //METODO -actualizarJugador- CON MODIFICACIONES PARA MANEJAR EL TAMANIO DE LOS OBJETOS
    public static void actualizarJugador(File fichero, Jugador j) throws IOException, ClassNotFoundException {
        long posicion = buscarJugadorFichero(fichero, j);
        if (posicion == -1) {
            throw new IllegalArgumentException("Jugador no encontrado en el fichero.");
        }

        try (RandomAccessFile raf = new RandomAccessFile(fichero, "rw")) {
            raf.seek(posicion);
            int longitudOriginal = raf.readInt();

            byte[] nuevosBytes = Convert.toBytes(j);

            if (nuevosBytes.length <= longitudOriginal) {
                // Cabe en el espacio original
                raf.seek(posicion + 4);
                raf.write(nuevosBytes);
                // Rellenar con ceros si es necesario
                if (nuevosBytes.length < longitudOriginal) {
                    int bytesRestantes = longitudOriginal - nuevosBytes.length;
                    byte[] padding = new byte[bytesRestantes];
                    raf.write(padding);
                }
            } else {
                // Si no cabe, reescribir todo el archivo
                raf.close(); // Cerramos antes de reescribir

                // Leer todos los jugadores
                ArrayList<Jugador> jugadores = obtenerJugadores(fichero);

                // Actualizar el jugador en la lista
                boolean encontrado = false;
                for (int i = 0; i < jugadores.size(); i++) {
                    if (jugadores.get(i).compareTo(j)) {
                        jugadores.set(i, j);
                        encontrado = true;
                        break;
                    }
                }

                if (!encontrado) {
                    throw new IllegalArgumentException("Jugador no encontrado en la lista (esto no debería pasar).");
                }

                // Reescribir el archivo
                reescribirArchivoCompleto(fichero, jugadores);
            }
        }
    }

    //Metodo para reescribir el fichero completamente
    private static void reescribirArchivoCompleto(File fichero, ArrayList<Jugador> jugadores) throws IOException {
        // Usar un archivo temporal para evitar corrupción en caso de error
        File tempFile = new File(fichero.getAbsolutePath() + ".tmp");

        try (RandomAccessFile rafTemp = new RandomAccessFile(tempFile, "rw")) {
            // Escribir encabezado
            rafTemp.writeInt(jugadores.size());

            // Escribir todos los jugadores
            for (Jugador jugador : jugadores) {
                byte[] jugadorBytes = Convert.toBytes(jugador);
                rafTemp.writeInt(jugadorBytes.length);
                rafTemp.write(jugadorBytes);
            }
        }

        // Reemplazar el archivo original
        if (fichero.exists() && !fichero.delete()) {
            throw new IOException("No se pudo eliminar el archivo original");
        }

        if (!tempFile.renameTo(fichero)) {
            throw new IOException("No se pudo renombrar el archivo temporal");
        }
    }

    /*public static void actualizarJugador(File fichero, Jugador j) throws IOException, ClassNotFoundException {
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

         */

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




    public static void eliminarJugador(File fichero, Jugador jugadorAEliminar) throws IOException, ClassNotFoundException {
        // Primero verificamos si el jugador existe
        long posicion = buscarJugadorFichero(fichero, jugadorAEliminar);
        if (posicion == -1) {
            throw new IllegalArgumentException("Jugador no encontrado en el fichero.");
        }

        RandomAccessFile raf = abrirRaf(fichero);

        try {
            // Leer el número total de jugadores
            raf.seek(0);
            int totalJugadores = raf.readInt();

            if (totalJugadores <= 0) {
                throw new IllegalArgumentException("No hay jugadores para eliminar.");
            }

            // Crear una lista temporal con todos los jugadores excepto el que vamos a eliminar
            ArrayList<Jugador> jugadoresRestantes = new ArrayList<>();

            // Volver al inicio después del encabezado
            raf.seek(4); // Saltar el entero del encabezado

            // Leer todos los jugadores y guardar los que no sean el que vamos a eliminar
            for (int i = 0; i < totalJugadores; i++) {
                long posicionActual = raf.getFilePointer();
                int longitud = raf.readInt();
                byte[] datosJugador = new byte[longitud];
                raf.read(datosJugador);
                Jugador jugadorActual = (Jugador) Convert.toObject(datosJugador);

                // Solo agregar a la lista si NO es el jugador a eliminar
                if (!jugadorActual.compareTo(jugadorAEliminar)) {
                    jugadoresRestantes.add(jugadorActual);
                }
            }

            // Cerrar el RAF antes de truncar el archivo
            raf.close();

            // Truncar el archivo a 0 bytes (eliminar todo el contenido)
            RandomAccessFile rafTruncado = new RandomAccessFile(fichero, "rw");
            rafTruncado.setLength(0);
            rafTruncado.close();

            // Volver a escribir el archivo con los jugadores restantes
            RandomAccessFile rafNuevo = abrirRaf(fichero);

            // Escribir el nuevo encabezado con el número de jugadores restantes
            rafNuevo.writeInt(jugadoresRestantes.size());

            // Escribir todos los jugadores restantes
            for (Jugador jugador : jugadoresRestantes) {
                byte[] jugadorBytes = Convert.toBytes(jugador);
                rafNuevo.writeInt(jugadorBytes.length);
                rafNuevo.write(jugadorBytes);
            }

            rafNuevo.close();

        } catch (IOException e) {
            if (raf != null) {
                raf.close();
            }
            throw e;
        }
    }




        private static RandomAccessFile abrirRaf(File fichero) throws FileNotFoundException {
            RandomAccessFile raf = new RandomAccessFile(fichero, "rw");
            return raf;
        }

    }
