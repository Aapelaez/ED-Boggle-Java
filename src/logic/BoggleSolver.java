package logic;

import cu.edu.cujae.ceis.graph.LinkedGraph;
import cu.edu.cujae.ceis.graph.vertex.Vertex;
import utils.SpanishWordHeuristics;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;

public class BoggleSolver {
    private final LinkedGraph graph;
    private final char[] letters;     // letras por índice 0..15
    private final int[][] neighbors;  // adyacencias por índice

    public BoggleSolver(LinkedGraph graph) {
        this.graph = graph;
        this.letters = extractLetters(graph);
        this.neighbors = extractNeighbors(graph);
    }

    private static char[] extractLetters(LinkedGraph g) {
        int n = BoggleBoard.ROWS * BoggleBoard.COLS; // 16
        char[] arr = new char[n];
        for (int i = 0; i < n; i++) {
            Vertex v = (Vertex) g.getVerticesList().get(i);
            BoggleCell cell = (BoggleCell) v.getInfo();
            arr[i] = cell.letter;
        }
        return arr;
    }

    private static int[][] extractNeighbors(LinkedGraph g) {
        int n = BoggleBoard.ROWS * BoggleBoard.COLS; // 16
        int[][] adj = new int[n][];
        for (int i = 0; i < n; i++) {
            LinkedList<Vertex> vs = g.adjacentsG(i);
            int[] idxs = new int[vs.size()];
            for (int k = 0; k < vs.size(); k++) {
                idxs[k] = g.getVertexIndex(vs.get(k));
            }
            adj[i] = idxs;
        }
        return adj;
    }

    // Valida una palabra normalizada (minúsculas, sin tildes, solo a–z) en el tablero
    public boolean canFormWord(String word) {
        if (word == null) return false;
        int len = word.length();
        if (len < 3) return false;

        char first = word.charAt(0);
        boolean[] visited = new boolean[letters.length];
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == first) {
                Arrays.fill(visited, false);
                if (dfs(i, word, 0, visited)) return true;
            }
        }
        return false;
    }

    private boolean dfs(int idx, String word, int pos, boolean[] visited) {
        if (letters[idx] != word.charAt(pos)) return false;
        if (pos == word.length() - 1) {
            return true;
        }
        visited[idx] = true;
        int nextPos = pos + 1;
        char nextChar = word.charAt(nextPos);
        for (int nb : neighbors[idx]) {
            if (!visited[nb] && letters[nb] == nextChar) {
                if (dfs(nb, word, nextPos, visited)) {
                    visited[idx] = false;
                    return true;
                }
            }
        }
        visited[idx] = false;
        return false;
    }

    // Encuentra todas las palabras del tablero usando el diccionario con poda por prefijo
    public Set<String> findAllWords(Dictionary dict, int minLen) {
        return findAllWords(dict, minLen, w -> true);
    }

    // Variante con filtro para listado (reduce ruido visual sin afectar validación de usuario)
    public Set<String> findAllWords(Dictionary dict, int minLen, Predicate<String> accept) {
        Set<String> results = new HashSet<>();
        boolean[] visited = new boolean[letters.length];
        StringBuilder sb = new StringBuilder(16);

        for (int start = 0; start < letters.length; start++) {
            Arrays.fill(visited, false);
            dfsEnumerate(start, dict, minLen, visited, sb, results, accept);
        }
        return results;
    }

    private void dfsEnumerate(int idx, Dictionary dict, int minLen, boolean[] visited, StringBuilder sb,
                              Set<String> out, Predicate<String> accept) {
        visited[idx] = true;
        sb.append(letters[idx]);
        String cur = sb.toString();

        // poda por prefijo de diccionario
        if (!dict.containsPrefix(cur)) {
            sb.setLength(sb.length() - 1);
            visited[idx] = false;
            return;
        }

        if (cur.length() >= minLen && dict.containsWord(cur)) {
            if (accept.test(cur)) {
                out.add(cur);
            }
        }

        for (int nb : neighbors[idx]) {
            if (!visited[nb]) {
                dfsEnumerate(nb, dict, minLen, visited, sb, out, accept);
            }
        }

        sb.setLength(sb.length() - 1);
        visited[idx] = false;
    }

    // Ayudante para listado “limpio”
    public Set<String> findAllWordsFiltered(Dictionary dict, int minLen) {
        return findAllWords(dict, minLen, SpanishWordHeuristics::acceptableForListing);
    }
}