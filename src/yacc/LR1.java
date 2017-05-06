package yacc;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class LR1 {
    private Productions productions;
    private Symbols symbols;
    private Map<Integer, HashSet<Integer>> firsts;
    private Set<ItemSet> collection;

    LR1() {
    }

    void parse(Productions productions, Symbols symbols) {
        this.productions = productions;
        this.symbols = symbols;

        constructFirsts();
        constructCollection();
    }

    private void constructFirsts() {
        firsts = new HashMap<>();
        for (Integer terminal : symbols.getTerminalIndexes()) {
            firsts.put(terminal, new HashSet<>(terminal));
        }

        // prodsBeginWith, productions whose body begins with a non-terminal
        // key: first non-terminal in the body
        // value: production's head
        HashMap<Integer, HashSet<Integer>> prodsBeginWith = new HashMap<>();
        HashMap<Integer, HashSet<Integer>> changed = new HashMap<>();

        for (Production prod : productions.getProductions()) {
            int first = prod.getBody().get(0);
            if (symbols.isTerminal(first)) {
                multiMapPut(firsts, prod.getHead(), first);
                multiMapPut(changed, prod.getHead(), first);
            } else {
                multiMapPut(prodsBeginWith, first, prod.getHead());
            }
        }

        HashMap<Integer, HashSet<Integer>> tmpChanged = new HashMap<>();
        while (!changed.isEmpty()) {
            for (Map.Entry<Integer, HashSet<Integer>> entry : changed.entrySet()) {
                for (Integer head : prodsBeginWith.get(entry.getKey())) {
                    for (Integer first : entry.getValue()) {
                        multiMapPut(firsts, head, first);
                        multiMapPut(tmpChanged, head, first);
                    }
                }
            }
            changed = tmpChanged;
            tmpChanged.clear();
        }
    }

    private void constructCollection() {
        collection = new HashSet<>();
        ItemSet initItemSet = new ItemSet();
        initItemSet.addItem(new Item(productions.getStart().getIndex(), 0));
        initItemSet.closure(productions, symbols, firsts);
        collection.add(initItemSet);


    }

    private <K, V> void multiMapPut(Map<K, HashSet<V>> multiMap, K key, V value) {
        multiMap.computeIfAbsent(key, k -> new HashSet<>());
        multiMap.get(key).add(value);
    }
}
