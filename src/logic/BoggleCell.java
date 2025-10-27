package logic;

/**
 * Información que se almacena como "info" de cada vértice del grafo.
 * Representa una celda del tablero de Boggle.
 */
public final class BoggleCell implements java.io.Serializable {
    public final int row;
    public final int col;
    public final int index; // row * 4 + col
    public final char letter;

    public BoggleCell(int row, int col, int index, char letter) {
        this.row = row;
        this.col = col;
        this.index = index;
        this.letter = letter;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ",'" + letter + "')";
    }
}