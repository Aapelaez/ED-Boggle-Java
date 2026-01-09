package logic;

import cu.edu.cujae.ceis.graph.LinkedGraph;
import cu.edu.cujae.ceis.graph.vertex.Vertex;

import java.util.LinkedList;

public final class BoggleGraphBuilder {

    private BoggleGraphBuilder() {}

    public static LinkedGraph build(TableroBoggle board) {
        LinkedGraph g = new LinkedGraph();

        // 1) Insertar 16 vértices con su info (CeldaTablero)
        for (int r = 0; r < TableroBoggle.ROWS; r++) {
            for (int c = 0; c < TableroBoggle.COLS; c++) {
                int idx = index(r, c);
                char letter = board.get(r, c);
                g.insertVertex(new CeldaTablero(r, c, idx, letter));
            }
        }

        // 2) Conectar aristas no dirigidas entre vecinos (8 direcciones)
        for (int r = 0; r < TableroBoggle.ROWS; r++) {
            for (int c = 0; c < TableroBoggle.COLS; c++) {
                int i = index(r, c);
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        if (dr == 0 && dc == 0) continue;
                        int nr = r + dr, nc = c + dc;
                        if (!inBounds(nr, nc)) continue;

                        int j = index(nr, nc);
                        if (i < j) {
                            boolean inserted = false;
                            try {
                                // Variante más común: por índices
                                inserted = g.insertEdgeNDG(i, j);
                            } catch (Throwable t) {
                                inserted = false;
                            }
                            if (!inserted) {
                                // Fallback: dos aristas dirigidas
                                g.insertEdgeDG(i, j);
                                g.insertEdgeDG(j, i);
                            }
                        }
                    }
                }
            }
        }

        return g;
    }

    public static int index(int row, int col) {
        return row * TableroBoggle.COLS + col;
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < TableroBoggle.ROWS && c >= 0 && c < TableroBoggle.COLS;
    }

    public static LinkedList<Vertex> obtenerAdyacentes(LinkedGraph g, int indice) {
        return g.adjacentsG(indice);
    }

    public static int[] neighborIndices(LinkedGraph g, int indice) {
        LinkedList<Vertex> vs = obtenerAdyacentes(g, indice);
        int[] idxs = new int[vs.size()];
        for (int k = 0; k < vs.size(); k++) {
            idxs[k] = g.getVertexIndex(vs.get(k));
        }
        return idxs;
    }

    public static Vertex obtenerVertice(LinkedGraph g, int pos) {
        return (Vertex) g.getVerticesList().get(pos);
    }

    public static CeldaTablero cellInfo(Vertex v) {
        return (CeldaTablero) v.getInfo();
    }
}