package logic;

import cu.edu.cujae.ceis.graph.LinkedGraph;
import cu.edu.cujae.ceis.graph.vertex.Vertex;

import java.util.LinkedList;

public final class BoggleGraphBuilder {

    private BoggleGraphBuilder() {}

    public static LinkedGraph build(BoggleBoard board) {
        LinkedGraph g = new LinkedGraph();

        // 1) Insertar 16 vértices con su info (BoggleCell)
        for (int r = 0; r < BoggleBoard.ROWS; r++) {
            for (int c = 0; c < BoggleBoard.COLS; c++) {
                int idx = index(r, c);
                char letter = board.get(r, c);
                g.insertVertex(new BoggleCell(r, c, idx, letter));
            }
        }

        // 2) Conectar aristas no dirigidas entre vecinos (8 direcciones)
        for (int r = 0; r < BoggleBoard.ROWS; r++) {
            for (int c = 0; c < BoggleBoard.COLS; c++) {
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
        return row * BoggleBoard.COLS + col;
    }

    private static boolean inBounds(int r, int c) {
        return r >= 0 && r < BoggleBoard.ROWS && c >= 0 && c < BoggleBoard.COLS;
    }

    public static LinkedList<Vertex> neighbors(LinkedGraph g, int cellIndex) {
        return g.adjacentsG(cellIndex);
    }

    public static int[] neighborIndices(LinkedGraph g, int cellIndex) {
        LinkedList<Vertex> vs = neighbors(g, cellIndex);
        int[] idxs = new int[vs.size()];
        for (int k = 0; k < vs.size(); k++) {
            idxs[k] = g.getVertexIndex(vs.get(k));
        }
        return idxs;
    }

    public static Vertex vertexAt(LinkedGraph g, int cellIndex) {
        // Tu JAR expone getVerticesList(); usamos ese método en lugar de verts(int)
        return (Vertex) g.getVerticesList().get(cellIndex);
    }

    public static BoggleCell cellInfo(Vertex v) {
        return (BoggleCell) v.getInfo();
    }
}