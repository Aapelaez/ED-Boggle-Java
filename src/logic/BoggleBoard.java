package logic;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Tablero 4x4 para Boggle con generador de letras ponderadas para español.
 * - Excluye 'ñ'
 * - Minúsculas
 */
public class BoggleBoard {

    public static final int ROWS = 4;
    public static final int COLS = 4;
    private static final Random RNG = new SecureRandom();

    private final char[][] grid = new char[ROWS][COLS];

    public BoggleBoard() {
        fillRandom();
    }

    public BoggleBoard(char[][] preset) {
        if (preset == null || preset.length != ROWS || preset[0].length != COLS) {
            throw new IllegalArgumentException("El tablero debe ser 4x4");
        }
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(preset[r], 0, this.grid[r], 0, COLS);
        }
    }

    public char get(int r, int c) {
        return grid[r][c];
    }

    public char[][] getGrid() {
        char[][] copy = new char[ROWS][COLS];
        for (int r = 0; r < ROWS; r++) {
            System.arraycopy(grid[r], 0, copy[r], 0, COLS);
        }
        return copy;
    }

    private void fillRandom() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                grid[r][c] = randomSpanishLetter();
            }
        }
    }

    // Distribución ponderada simple para español (ajústala si quieres)
    private static final char[] LETTERS = (
            "aaaaaaa" +       // a
                    "bbbb" +          // b
                    "cccccc" +        // c
                    "dddddd" +        // d
                    "eeeeeeeeee" +    // e
                    "ffff" +          // f
                    "gggg" +          // g
                    "hhhh" +          // h
                    "iiiiiiii" +      // i
                    "jj" +            // j
                    "kk" +            // k (rara)
                    "lllllll" +       // l
                    "mmmmmmm" +       // m
                    "nnnnnnnn" +      // n (sin ñ)
                    "oooooooo" +      // o
                    "pppppp" +        // p
                    "q" +             // q (rara)
                    "rrrrrrrr" +      // r
                    "ssssssss" +      // s
                    "tttttttt" +      // t
                    "uuuuuu" +        // u
                    "vv" +            // v
                    "ww" +            // w (rara)
                    "x" +             // x (rara)
                    "yyyy" +          // y
                    "z"               // z
    ).toCharArray();

    private static char randomSpanishLetter() {
        return LETTERS[RNG.nextInt(LETTERS.length)];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(ROWS * (COLS * 2));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                sb.append(grid[r][c]).append(' ');
            }
            if (r < ROWS - 1) sb.append('\n');
        }
        return sb.toString();
    }
}