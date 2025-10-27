package utils;

import logic.Dictionary;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

public final class DictionaryLoader {
    private DictionaryLoader() {}

    public static void loadIntoDictionary(String resourcePath, Dictionary dict) throws IOException {
        loadWithOptions(resourcePath, dict, true, true, true, false); // requireVowel=false por defecto
    }

    public static void loadWithOptions(
            String resourcePath,
            Dictionary dict,
            boolean excludeAllCapsTokens,
            boolean excludeProperNouns,
            boolean excludePunctuatedTokens,
            boolean requireVowel // NUEVA opción
    ) throws IOException {

        InputStream is = DictionaryLoader.class.getClassLoader().getResourceAsStream(resourcePath);
        if (is == null) {
            throw new FileNotFoundException("No se encontró el recurso en el classpath: " + resourcePath);
        }

        Set<String> uniqueSorted = new TreeSet<>();
        int skippedEnye = 0, skippedAllCaps = 0, skippedProper = 0, skippedPunct = 0, skippedTriples = 0, skippedNoVowel = 0, total = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String raw = line.trim();
                if (raw.isEmpty()) continue;
                if (containsEnye(raw)) { skippedEnye++; continue; }
                if (excludeAllCapsTokens && isAllCapsToken(raw)) { skippedAllCaps++; continue; }
                if (excludePunctuatedTokens && hasPunctuation(raw)) { skippedPunct++; continue; }
                if (excludeProperNouns && isProperNoun(raw)) { skippedProper++; continue; }

                String normalized = normalize(raw);
                if (normalized.length() < 3) continue;
                if (hasTripleRepeat(normalized)) { skippedTriples++; continue; }

                if (requireVowel && !hasVowel(normalized)) { skippedNoVowel++; continue; }

                uniqueSorted.add(normalized);
            }
        }

        List<String> words = new ArrayList<>(uniqueSorted);
        insertBalanced(dict, words, 0, words.size() - 1);

        System.out.printf(
                "Diccionario: total lineas=%d | cargadas=%d | ñ=%d | SIGLAS=%d | PUNTOS=%d | PROPIOS=%d | triples=%d | sinVocal=%d%n",
                total, dict.size(), skippedEnye, skippedAllCaps, skippedPunct, skippedProper, skippedTriples, skippedNoVowel
        );
    }

    private static void insertBalanced(Dictionary dict, List<String> words, int lo, int hi) {
        if (lo > hi) return;
        int mid = (lo + hi) >>> 1;
        String w = words.get(mid);
        try { dict.insert(w); } catch (RuntimeException ignore) {}
        insertBalanced(dict, words, lo, mid - 1);
        insertBalanced(dict, words, mid + 1, hi);
    }

    private static boolean containsEnye(String s) { return s.indexOf('ñ') >= 0 || s.indexOf('Ñ') >= 0; }
    private static String normalize(String input) {
        String lower = input.toLowerCase();
        String nfd = Normalizer.normalize(lower, Normalizer.Form.NFD);
        String withoutDiacritics = nfd.replaceAll("\\p{M}+", "");
        return withoutDiacritics.replaceAll("[^a-z]", "");
    }
    private static boolean hasTripleRepeat(String s) {
        int run = 1;
        for (int i = 1; i < s.length(); i++) {
            if (s.charAt(i) == s.charAt(i - 1)) { if (++run >= 3) return true; }
            else run = 1;
        }
        return false;
    }
    private static boolean isAllCapsToken(String raw) {
        if (raw.length() < 2) return false;
        if (raw.matches("^[A-ZÁÉÍÓÚÜÑ .\\-\\+/]+$")) {
            int upper = 0, letters = 0;
            for (int i = 0; i < raw.length(); i++) {
                char ch = raw.charAt(i);
                if (Character.isLetter(ch)) { letters++; if (Character.isUpperCase(ch)) upper++; }
            }
            return letters >= 2 && upper >= Math.max(2, (int)Math.round(letters * 0.8));
        }
        return false;
    }
    private static boolean isProperNoun(String raw) { return raw.matches("^[A-ZÁÉÍÓÚÜ][a-záéíóúü]+$"); }
    private static boolean hasPunctuation(String raw) { return raw.matches(".*[\\./\\-\\+_0-9].*"); }
    private static boolean hasVowel(String w) {
        for (int i = 0; i < w.length(); i++) {
            char ch = w.charAt(i);
            if (ch=='a'||ch=='e'||ch=='i'||ch=='o'||ch=='u') return true;
        }
        return false;
    }
}