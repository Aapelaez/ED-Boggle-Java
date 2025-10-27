package logic;

public interface Dictionary {
    void insert(String word);
    boolean containsWord(String word);
    boolean containsPrefix(String prefix);
    int size();
}