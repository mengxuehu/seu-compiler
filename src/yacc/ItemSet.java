package yacc;


import java.util.*;

class ItemSet {
    private int index;
    private Set<Item> items;
    private static int indexes = 0;

    ItemSet() {
        this.index = indexes++;
        items = new TreeSet<>();
    }

    ItemSet(Collection<Item> itemCollections) {
        this.index = indexes++;
        items = new TreeSet<>(itemCollections);
    }

    boolean addItem(Item item) {
        return items.add(item);
        // TODO: merge
    }

    int getIndex() {
        return index;
    }

    Set<Item> getItems() {
        return items;
    }

    void closure(Productions productions, Symbols symbols, Map<Integer, HashSet<Integer>> firsts) {
        boolean flag = true;
        ArrayList<Item> tmpItemSet = new ArrayList<>();
        // TODO: indexes-- ?
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
        int result = index;
        result = 31 * result + (items != null ? items.hashCode() : 0);
        return result;
    }
}
