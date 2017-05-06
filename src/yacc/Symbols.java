package yacc;


import java.util.*;

class Symbols {
    //    private ArrayList<String> symbolsArray;
    private HashSet<Integer> terminalIndexes, nonTerminalIndexes;//, symbolIndexes;
    private HashMap<String, Integer> symbols;
    private int indexes;
    private int startAug;


    Symbols() {
//        symbolsArray = new ArrayList<>();
        terminalIndexes = new HashSet<>();
        nonTerminalIndexes = new HashSet<>();
//        symbolIndexes = new HashSet<>();
        symbols = new HashMap<>();
        indexes = 1000;
        startAug = indexes++;
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

    boolean isTerminal(int terminalIndex) {
        return terminalIndexes.contains(terminalIndex);
    }

    boolean isNonTerminal(int nonTerminalIndex) {
        return nonTerminalIndexes.contains(nonTerminalIndex);
    }


    boolean contains(String symbol) {
//        return symbolsArray.contains(symbol);
        return symbols.containsKey(symbol);
    }

    public int getStartAug() {
        return startAug;
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
//        return symbolIndexes;
        return symbols.values();
    }

    Map<String, Integer> getSymbols() {
        return symbols;
    }

    private int addSymbol(HashSet<Integer> symbolIndexes_, String symbol, int idx) {
//        if (!symbolsArray.contains(symbol)) {
//            indexes.add(symbolsArray.size());
//            symbolIndexes.add(symbolsArray.size());
//            symbolsArray.add(symbol);
//            return true;
//        }
        if (!symbols.containsKey(symbol)) {
            symbolIndexes_.add(idx);
            symbols.put(symbol, idx);
            return idx;
        }
        return -1;
    }
}
