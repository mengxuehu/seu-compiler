package yacc.entity;


import java.util.*;

public class Symbols {
    private HashSet<Integer> terminalIndexes, nonTerminalIndexes;
    private HashMap<String, Integer> symbols;
    private HashMap<Integer, String> invertedSymbols;
    private int indexes;
    final static private int START_AUG = -3;
    final static private int END = -2;
    final static private int ERROR = -1;


    public Symbols() {
        terminalIndexes = new HashSet<>();
        nonTerminalIndexes = new HashSet<>();
        symbols = new HashMap<>();
        invertedSymbols = new HashMap<>();
        indexes = 257;
    }

    public int addTerminal(String terminal) {
        return addSymbol(terminalIndexes, terminal, indexes++);
    }

    private int addSymbol(HashSet<Integer> symbolIndexes_, String symbol, int idx) {
        if (!symbols.containsKey(symbol)) {
            symbolIndexes_.add(idx);
            symbols.put(symbol, idx);
            invertedSymbols.put(idx, symbol);
            return idx;
        }
        return -1;
    }

    public int addNonTerminal(String nonTerminal) {
        return addSymbol(nonTerminalIndexes, nonTerminal, indexes++);
    }

    public int addChar(char ch) {
        return addSymbol(terminalIndexes, String.valueOf(ch), (int) ch);
    }

    // TODO? end
    public boolean isTerminal(int symbolIndex) {
        return terminalIndexes.contains(symbolIndex);
    }

    // TODO? START_AUG
    boolean isNonTerminal(int symbolIndex) {
        return nonTerminalIndexes.contains(symbolIndex);
    }

    public boolean contains(String symbol) {
        return symbols.containsKey(symbol);
    }

    public int getStartAug() {
        return START_AUG;
    }

    public int getEnd() {
        return END;
    }

    public int getError() {
        return ERROR;
    }

    public int getSymbolIndex(String symbol) {
        return symbols.get(symbol);
    }

    public String getInvertedSymbol(int index) {
        return invertedSymbols.get(index);
    }

    public Set<Integer> getTerminalIndexes() {
        return terminalIndexes;
    }

    public Set<Integer> getNonTerminalIndexes() {
        return nonTerminalIndexes;
    }

    public Collection<Integer> getSymbolIndexes() {
        return symbols.values();
    }

    public Map<String, Integer> getSymbols() {
        return symbols;
    }

    public Map<Integer, String> getInvertedSymbols() {
        return invertedSymbols;
    }
}
