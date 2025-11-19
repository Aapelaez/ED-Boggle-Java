package logic;

import utils.TextNormalizer;

/**
 * Valida palabras ingresadas por el usuario:
 * 1) Normaliza (minúsculas, sin tildes, solo a-z).
 * 2) Aplica reglas del juego (longitud >= 3, sin 'ñ').
 * 3) Verifica que se puede formar en el tablero con el solver.
 * 4) Verifica que existe en el diccionario (Dictionary.containsWord).
 */
public class GameWordValidator {

    public enum Result {
        OK,
        TOO_SHORT,
        INVALID_CHARACTERS, // incluye 'ñ' o caracteres que no sean letras
        NOT_FORMABLE_ON_BOARD,
        NOT_IN_DICTIONARY
    }

    private final BoggleSolver solver;
    private final Dictionary dictionary;

    public GameWordValidator(BoggleSolver solver, Dictionary dictionary) {
        this.solver = solver;
        this.dictionary = dictionary;
    }

    public static final class Validation {
        public final Result result;
        public final String normalized;

        private Validation(Result result, String normalized) {
            this.result = result;
            this.normalized = normalized;
        }

        public boolean isOk() { return result == Result.OK; }
    }

    public Validation validateUserWord(String rawInput) {
        if (rawInput == null) return new Validation(Result.INVALID_CHARACTERS, "");

        // Detecta 'ñ' explícitamente antes de normalizar (según la regla del juego)
        if (rawInput.indexOf('ñ') >= 0 || rawInput.indexOf('Ñ') >= 0) {
            return new Validation(Result.INVALID_CHARACTERS, "");
        }

        // Si solo tiene espacios o está vacío -> caracteres inválidos
        if (rawInput.trim().isEmpty()) {
            return new Validation(Result.INVALID_CHARACTERS, "");
        }

        // Si contiene cualquier carácter que no sea letra (por ejemplo dígitos, signos de puntuación)
        // consideramos inválido. Permitimos espacios (aunque los rechazamos arriba si solo hay espacios).
        for (int i = 0; i < rawInput.length(); i++) {
            char ch = rawInput.charAt(i);
            if (Character.isWhitespace(ch)) continue; // espacios son tolerados (se quitan al normalizar)
            if (!Character.isLetter(ch)) {
                return new Validation(Result.INVALID_CHARACTERS, "");
            }
        }

        // Normaliza: minusculas, sin tildes, solo [a-z]
        String norm = TextNormalizer.normalize(rawInput);

        // Por seguridad, si tras normalizar se perdió todo
        if (norm.isEmpty()) {
            return new Validation(Result.INVALID_CHARACTERS, "");
        }

        if (norm.length() < 3) {
            return new Validation(Result.TOO_SHORT, norm);
        }

        // 1) Verificar que se puede formar en el tablero
        if (!solver.canFormWord(norm)) {
            return new Validation(Result.NOT_FORMABLE_ON_BOARD, norm);
        }

        // 2) Verificar en diccionario
        if (!dictionary.containsWord(norm)) {
            return new Validation(Result.NOT_IN_DICTIONARY, norm);
        }

        return new Validation(Result.OK, norm);
    }
}