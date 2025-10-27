package utils;

import java.text.Normalizer;

public final class TextNormalizer {
    private TextNormalizer() {}

    // Normaliza a minúsculas, quita tildes/diacríticos y deja solo [a-z]
    public static String normalize(String input) {
        if (input == null) return "";
        String lower = input.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String withoutDiacritics = nfd.replaceAll("\\p{M}+", ""); // elimina marcas combinadas
        String onlyLetters = withoutDiacritics.replaceAll("[^a-z]", ""); // descarta no letras
        return onlyLetters;
    }
}