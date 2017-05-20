package yacc;


import javafx.util.Pair;

import java.util.*;

class LR1 {
    private Productions productions;
    private Symbols symbols;
    private Map<Integer, HashSet<Integer>> firsts;
    private Set<ItemSet> collection;

    private static int ItemSetStateIndexes = 0;

    private Map<Pair<Integer, Integer>, Integer> tableGoto;
    private Map<Pair<Integer, Integer>, Action> tableAction;

    LR1() {
    }

    void parse(Productions productions, Symbols symbols) {
        this.productions = productions;
        this.symbols = symbols;

        firsts = new HashMap<>();
        tableGoto = new HashMap<>();
        tableAction = new HashMap<>();

        constructFirsts();
        constructCollection();
        constructAction();
    }

    private void constructFirsts() {
        for (Integer terminal : symbols.getTerminalIndexes()) {
            multiMapPut(firsts, terminal, terminal);
//            HashSet<Integer> tmp = new HashSet<>(1);
//            tmp.add(terminal);
//            HashSet<Integer> put = firsts.put(terminal, tmp);
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
                if (prodsBeginWith.containsKey(entry.getKey())) {
                    for (Integer head : prodsBeginWith.get(entry.getKey())) {
                        for (Integer first : entry.getValue()) {
                            Set tmp = firsts.get(head);
                            if (tmp == null || !tmp.contains(first)) {
                                multiMapPut(firsts, head, first);
                                multiMapPut(tmpChanged, head, first);
                            }
                        }
                    }
                }
            }
            changed = tmpChanged;
            tmpChanged = new HashMap<>();
        }
    }

    private void constructCollection() {
        collection = new HashSet<>();
        ItemSet initItemSet = new ItemSet(ItemSetStateIndexes++);
        Item item = new Item(productions.getStart().getIndex(), 0);
        item.addLookaheadSymbol(symbols.getEnd());
        initItemSet.addItem(item);
        initItemSet.closure(productions, symbols, firsts);
        collection.add(initItemSet);

        ArrayList<ItemSet> tmpCollection = new ArrayList<>(collection);
        for (int i = 0; i < tmpCollection.size(); i++) {
            for (Integer j : symbols.getSymbolIndexes()) {
                if (symbols.getEnd() != j) {
                    ItemSet itemSet = tmpCollection.get(i).goto_(j, productions, symbols, firsts);
                    if (itemSet == null) {
                        continue;
                    }
                    if (!collection.contains(itemSet)) {
                        itemSet.setState(ItemSetStateIndexes++);
                        collection.add(itemSet);
                        tmpCollection.add(itemSet);
                    } else {
                        for (ItemSet is : collection) {
                            if (is.equals(itemSet)) {
                                itemSet = is;
                                break;
                            }
                        }
                    }
                    if (symbols.isTerminal(j)) {
                        tableAction.put(new Pair<>(i, j), new ShiftAction(itemSet.getState()));
                    }
                    int state = tmpCollection.get(i).getState();
                    if (symbols.isTerminal(j)) {
                        tableAction.put(new Pair<>(state, j), new ShiftAction(itemSet.getState()));
                    } else {
                        tableGoto.put(new Pair<>(state, j), itemSet.getState());
                    }
                }
            }
        }
    }

    private void constructAction() {
        for (ItemSet itemSet : collection) {
            for (Item item : itemSet.getItems()) {
                Production production = productions.getProduction(item.getProductionIndex());

                // reduce or accept
                if (item.getPosition() == production.getBody().size()) {
                    if (production.getHead() == symbols.getStartAug()
                            && item.getLookaheadSymbols().contains(symbols.getEnd())) {
                        Pair<Integer, Integer> key = new Pair<>(itemSet.getState(), symbols.getEnd());
                        Action acceptAction = new AcceptAction();
                        if (exists(key)) {
                            tableAction.put(key, acceptAction);
                        }
                    } else {
                        Action reduceAction = new ReduceAction(item.getProductionIndex());
                        for (Integer lookahead : item.getLookaheadSymbols()) {
                            Pair<Integer, Integer> key = new Pair<>(itemSet.getState(), lookahead);
                            if (exists(key)) {
                                tableAction.put(key, reduceAction);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean exists(Pair<Integer, Integer> key) {
        Action oldAction = tableAction.get(key);
        if (oldAction != null) {
            if (oldAction.getType() == ActionType.SHIFT) {
                System.err.println("WARNING: shift/reduce conflict, shift will be taken");
            } else {
                System.err.println("WARNING: reduce/reduce conflict, the former reduce will be taken");
            }
            return false;
        } else {
            return true;
        }
    }

    Set<ItemSet> getCollection() {
        return collection;
    }

    Map<Pair<Integer, Integer>, Integer> getTableGoto() {
        return tableGoto;
    }

    Map<Pair<Integer, Integer>, Action> getTableAction() {
        return tableAction;
    }

    private <K, V> void multiMapPut(Map<K, HashSet<V>> multiMap, K key, V value) {
        multiMap.computeIfAbsent(key, k -> new HashSet<>());
        multiMap.get(key).add(value);
    }
}
