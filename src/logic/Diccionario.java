package logic;

public interface Diccionario {
    void insert(String word);
    boolean containsWord(String word);
    boolean containsPrefix(String prefix);
    int size();
}