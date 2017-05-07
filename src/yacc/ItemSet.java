package yacc;

import java.util.*;

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

    void closure(Productions productions, Symbols symbols, Map<Integer, HashSet<Integer>> firsts) {
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

    ItemSet goto_(int symbol, Productions productions, Symbols symbols, Map<Integer, HashSet<Integer>> firsts) {
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
        itemSet.closure(productions, symbols, firsts);
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

    boolean equalItemSet(ItemSet itemSet) {

        Iterator<Item> il = items.iterator(), ir = itemSet.items.iterator();
        while (il.hasNext()) {
            if (!il.next().equals(ir.next())) {
                return false;
            }
        }
        return true;
    }
}