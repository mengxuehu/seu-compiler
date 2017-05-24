package yacc.lr;

import yacc.entity.Production;
import yacc.entity.Productions;
import yacc.entity.Symbols;

import java.util.*;

class ItemSet implements Comparable<ItemSet> {
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

    int getState() {
        return state;
    }

    void setState(int state) {
        this.state = state;
    }

    Set<Item> getItems() {
        return items;
    }

    ItemSet goto_(int symbol, Productions productions, Symbols symbols, Map<Integer, HashSet<Integer>> firsts) {
        ItemSet itemSet = new ItemSet();
        for (Item item : this.items) {
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

    boolean addItem(Item item) {
        for (Item i : items) {
            if (i.equals(item)) {
                return i.addAllLookaheadSymbols(item.getLookaheadSymbols());
            }
        }
        return items.add(item);
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
//                if (item.getPosition() == body.size() - 1) {
//                    item.getLookaheadSymbols();
//                } else {
//                    System.out.println(body.get(item.getPosition() + 1));
//                    for (Map.Entry<String, Integer> entry : symbols.getSymbols().entrySet()) {
//                        if (entry.getValue().equals(body.get(item.getPosition() + 1))) {
//                            System.out.println(entry.getKey());
//                        }
//                    }
//                    System.out.println(firsts.get(body.get(item.getPosition() + 1)));
//                }
                tmpLookaheadSymbols.addAll((item.getPosition() == body.size() - 1)
                        ? item.getLookaheadSymbols()
                        : firsts.get(body.get(item.getPosition() + 1)));
                for (Production production : productions.getProductions()) {
                    if (production.getHead() == nonTerminal) {
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
    }

    @Override
    public int compareTo(ItemSet o) {
        return Integer.compare(state, o.state);
    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
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

    boolean equalItemSet(ItemSet itemSet) {
        if (this.items.size() != itemSet.items.size()) {
            return false;
        }
        Iterator<Item> il = items.iterator(), ir = itemSet.items.iterator();
        while (il.hasNext()) {
            if (!il.next().equals(ir.next())) {
                return false;
            }
        }
        return true;
    }
}