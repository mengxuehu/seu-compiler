package yacc;


import java.util.*;

class Symbols {
    private HashSet<Integer> terminalIndexes, nonTerminalIndexes;
    private HashMap<String, Integer> symbols;
    private int indexes;
    private int startAug;
    private int end;


    Symbols() {
        terminalIndexes = new HashSet<>();
        nonTerminalIndexes = new HashSet<>();
        symbols = new HashMap<>();
        indexes = 1000;

        startAug = indexes++;
        end = indexes++;
    }

    int addTerminal(String terminal) {
        return addSymbol(terminalIndexes, terminal, indexes++);
    }

    int addNonTerminal(String nonTerminal) {
        return addSymbol(nonTerminalIndexes, nonTerminal, indexes++);
    }

    int addChar(char ch) {
        return addSymbol(terminalIndexes, String.valueOf(ch), (int) ch);
    }

    boolean isTerminal(int symbolIndex) {
        return symbolIndex == end || terminalIndexes.contains(symbolIndex);
    }

    boolean isNonTerminal(int symbolIndex) {
        return !isTerminal(symbolIndex);
    }


    boolean contains(String symbol) {
        return symbols.containsKey(symbol);
    }

    int getStartAug() {
        return startAug;
    }

    int getEnd() {
        return end;
    }

    int getSymbolIndex(String symbol) {
        return symbols.get(symbol);
    }

    Set<Integer> getTerminalIndexes() {
        return terminalIndexes;
    }

    Set<Integer> getNonTerminalIndexes() {
        return nonTerminalIndexes;
    }

    Collection<Integer> getSymbolIndexes() {
        return symbols.values();
    }

    Map<String, Integer> getSymbols() {
        return symbols;
    }

    private int addSymbol(HashSet<Integer> symbolIndexes_, String symbol, int idx) {
        if (!symbols.containsKey(symbol)) {
            symbolIndexes_.add(idx);
            symbols.put(symbol, idx);
            return idx;
        }
        return -1;
    }
}
