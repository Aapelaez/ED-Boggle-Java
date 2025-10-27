package utils;

public final class SpanishWordHeuristics {
    private SpanishWordHeuristics() {}

    // Considera aeiou como vocales. Si quieres tratar 'y' como vocal en posición final, ajusta aquí.
    private static boolean hasVowel(String w) {
        for (int i = 0; i < w.length(); i++) {
            char ch = w.charAt(i);
            if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                return true;
            }
        }
        return false;
    }

    // Regla “cc” válida solo si va seguida de e o i (acción, accidente, etc.)
    private static boolean validCcContext(String w) {
        int idx = w.indexOf("cc");
        while (idx >= 0) {
            int next = idx + 2;
            if (next >= w.length()) return false; // termina en "cc"
            char after = w.charAt(next);
            if (after != 'e' && after != 'i') return false;
            idx = w.indexOf("cc", idx + 1);
        }
        return true;
    }

    // Heurística suave para listado: al menos una vocal y contexto “cc” correcto.
    public static boolean acceptableForListing(String w) {
        if (w == null || w.length() < 3) return false;
        if (!hasVowel(w)) return false;
        if (!validCcContext(w)) return false;
        return true;
    }
}