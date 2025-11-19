package logic;

import cu.edu.cujae.ceis.tree.general.GeneralTree;
import cu.edu.cujae.ceis.tree.binary.BinaryTreeNode;

public class TrieDictionary implements Dictionary {

    private static final class TrieValue {
        final char c;
        boolean isWord;
        TrieValue(char c, boolean isWord) { this.c = c; this.isWord = isWord; }
    }

    private final GeneralTree<TrieValue> tree;
    private final BinaryTreeNode<TrieValue> root;

    // contador real de palabras almacenadas
    private int wordCount = 0;

    public TrieDictionary() {
        // La nueva versión de GeneralTree ya no tiene constructor que acepte directamente
        // un valor; pide un BinaryTreeNode. Creamos el nodo raíz y lo pasamos.
        BinaryTreeNode<TrieValue> rootNode = new BinaryTreeNode<>(new TrieValue('\0', false));
        this.tree = new GeneralTree<>(rootNode);
        this.root = rootNode;
    }

    @Override
    public void insert(String word) {
        if (word == null || word.isEmpty()) return;
        BinaryTreeNode<TrieValue> cur = root;
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            BinaryTreeNode<TrieValue> child = findChildWithChar(cur, ch);
            if (child == null) {
                child = new BinaryTreeNode<>(new TrieValue(ch, false));
                addAsLastChild(cur, child);
            }
            cur = child;
        }
        TrieValue v = cur.getInfo();
        if (!v.isWord) {
            v.isWord = true;
            // cur.setInfo(v); // innecesario, v es mutable
            wordCount++;
        }
    }

    @Override
    public boolean containsWord(String word) {
        BinaryTreeNode<TrieValue> n = walk(word);
        return n != null && n.getInfo().isWord;
    }

    @Override
    public boolean containsPrefix(String prefix) {
        return walk(prefix) != null;
    }

    @Override
    public int size() {
        return wordCount;
    }

    private BinaryTreeNode<TrieValue> walk(String s) {
        if (s == null) return null;
        BinaryTreeNode<TrieValue> cur = root;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            cur = findChildWithChar(cur, ch);
            if (cur == null) return null;
        }
        return cur;
    }

    private BinaryTreeNode<TrieValue> findChildWithChar(BinaryTreeNode<TrieValue> parent, char ch) {
        BinaryTreeNode<TrieValue> child = parent.getLeft(); // primer hijo
        while (child != null) {
            if (child.getInfo().c == ch) return child;
            child = child.getRight(); // hermano derecho
        }
        return null;
    }

    private void addAsLastChild(BinaryTreeNode<TrieValue> parent, BinaryTreeNode<TrieValue> newChild) {
        BinaryTreeNode<TrieValue> first = parent.getLeft();
        if (first == null) {
            parent.setLeft(newChild);
        } else {
            BinaryTreeNode<TrieValue> it = first;
            while (it.getRight() != null) {
                it = it.getRight();
            }
            it.setRight(newChild);
        }
    }
}