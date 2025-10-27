import cu.edu.cujae.ceis.graph.LinkedGraph;
import logic.*;
import utils.DictionaryLoader;

import java.io.IOException;
import java.util.Set;

// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws IOException {
        // Tablero preconfigurado 4x4
        char[][] preset = {
                {'c','a','n','n'},
                {'x','c','i','o'},
                {'s','o','l','x'},
                {'m','a','d','e'}
        };
        BoggleBoard board = new BoggleBoard();
        System.out.println("Tablero (preconfigurado):");
        System.out.println(board);

        // Grafo
        LinkedGraph g = BoggleGraphBuilder.build(board);

        // Diccionario
        Dictionary dict = new TrieDictionary();
        DictionaryLoader.loadIntoDictionary("game_files/diccionario.txt", dict);
        System.out.println("Diccionario cargado: " + dict.size());

        // Solver y validador
        BoggleSolver solver = new BoggleSolver(g);
        GameWordValidator validator = new GameWordValidator(solver, dict);

        // Palabras de prueba
        String[] pruebas = {"canción", "sol", "amiga"};
        for (String w : pruebas) {
            var v = validator.validateUserWord(w);
            System.out.printf("%-10s -> %-24s (normalizada: '%s')%n", w, v.result, v.normalized);
        }

        // Opcional: listar todas las palabras del tablero (>=3 letras)
        Set<String> all = solver.findAllWords(dict, 3);
        System.out.println("\nPalabras encontradas (>=3): " + all.size());
        // Imprime algunas para muestra
        all.stream().limit(50).sorted().forEach(s -> System.out.print(s + " "));
        System.out.println();
    }
}
