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
    }

    private void constructFirsts() {
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
        collection = new TreeSet<>();
        ItemSet initItemSet = new ItemSet(ItemSetStateIndexes++);
        Item item = new Item(productions.getStart().getIndex(), 0);
        item.addLookaheadSymbol(symbols.getEnd());
        initItemSet.addItem(item);
        initItemSet.closure();
        collection.add(initItemSet);

        ArrayList<ItemSet> tmpCollection = new ArrayList<>(collection);
        for (int i = 0; i < tmpCollection.size(); i++) {
            for (Integer j : symbols.getSymbolIndexes()) {
                if (symbols.getEnd() != j) {
                    ItemSet itemSet = tmpCollection.get(i).goto_(j);
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

    public Set<ItemSet> getCollection() {
        return collection;
    }

    public Map<Pair<Integer, Integer>, Integer> getTableGoto() {
        return tableGoto;
    }

    public Map<Pair<Integer, Integer>, Action> getTableAction() {
        return tableAction;
    }

    private <K, V> void multiMapPut(Map<K, HashSet<V>> multiMap, K key, V value) {
        multiMap.computeIfAbsent(key, k -> new HashSet<>());
        multiMap.get(key).add(value);
    }

    class ItemSet implements Comparable<ItemSet>{
        private Integer state;
        private Set<Item> items;


        ItemSet() {
            this.state = null;
            items = new TreeSet<>();
        }

        ItemSet(int index) {
            this.state = index;
            items = new TreeSet<>();
        }

        boolean addItem(Item item) {
            for (Item i : items) {
                if (i.equals(item)) {
                    return i.addAllLookaheadSymbols(item.getLookaheadSymbols());
                }
            }
            return items.add(item);
        }

        void setState(int state) {
            this.state = state;
        }

        int getState() {
            return state;
        }

        Set<Item> getItems() {
            return items;
        }

        void closure() {
            if (items.isEmpty()) {
                return;
            }
            boolean flag = true;
            ArrayList<Item> tmpItemSet = new ArrayList<>();
            tmpItemSet.addAll(items);

            while (flag) {
                flag = false;
                for (int t = 0; t < tmpItemSet.size(); t++) {
                    Item item = tmpItemSet.get(t);
                    List<Integer> body = productions.getProduction(item.getProductionIndex()).getBody();
                    int nonTerminal;
                    if (item.getPosition() == body.size()
                            || symbols.isTerminal(nonTerminal = body.get(item.getPosition()))) {
                        continue;
                    }
                    HashSet<Integer> tmpLookaheadSymbols = new HashSet<>();
                    tmpLookaheadSymbols.addAll((item.getPosition() == body.size() - 1)
                            ? item.getLookaheadSymbols()
                            : firsts.get(body.get(item.getPosition() + 1)));
                    for (Production production : productions.getProductions()) {
                        if (production.getHead() != nonTerminal) {
                            continue;
                        }
                        Item item1 = new Item(production.getIndex(), 0);
                        item1.addAllLookaheadSymbols(tmpLookaheadSymbols);
                        if (addItem(item1)) {
                            tmpItemSet.add(item1);
                            flag = true;
                        }
                    }
                }
            }
        }

        ItemSet goto_(int symbol) {
            ItemSet itemSet = new ItemSet();
            for (Item item : itemSet.items) {
                List<Integer> body = productions.getProductions().get(item.getProductionIndex()).getBody();
                if (item.getPosition() == body.size() || body.get(item.getPosition()) != symbol) {
                    continue;
                }
                Item i = new Item(item.getProductionIndex(), item.getPosition() + 1);
                i.addAllLookaheadSymbols(item.getLookaheadSymbols());
                itemSet.addItem(i);
            }
            itemSet.closure();
            return (itemSet.items.isEmpty() ? null : itemSet);
        }

        @Override
        public int compareTo(ItemSet o) {
            return Integer.compare(state, o.state);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ItemSet itemSet = (ItemSet) o;
            if (items == null || itemSet.items == null) {
                return items == null && itemSet.items == null;
            }
            if (items.size() != itemSet.items.size()) {
                return false;
            }
            Iterator<Item> il = items.iterator(), ir = itemSet.items.iterator();
            while (il.hasNext()) {
                if (il.next().compareTo(ir.next()) != 0) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = state;
            result = 31 * result + (items != null ? items.hashCode() : 0);
            return result;
        }

        public boolean equalItemSet(ItemSet itemSet) {

            Iterator<Item> il = items.iterator(), ir = itemSet.items.iterator();
            while (il.hasNext()) {
                if (il.next().compareTo(ir.next()) != 0) {
                    return false;
                }
            }
            return true;
        }
    }
}
