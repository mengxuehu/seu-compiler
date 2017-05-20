package yacc;


import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

class Item implements Comparable<Item> {
    private int productionIndex;
    private int position;
    private Set<Integer> lookaheadSymbols;

    Item(int productionIndex, int position) {
        this.productionIndex = productionIndex;
        this.position = position;
        this.lookaheadSymbols = new TreeSet<>(Integer::compareTo);
    }

    int getProductionIndex() {
        return productionIndex;
    }

    int getPosition() {
        return position;
    }

    boolean addAllLookaheadSymbols(Collection<Integer> lookaheadSymbols) {
        return this.lookaheadSymbols.addAll(lookaheadSymbols);
    }

    boolean addLookaheadSymbol(int lookaheadSymbol) {
        return this.lookaheadSymbols.add(lookaheadSymbol);
    }

    Set<Integer> getLookaheadSymbols() {
        return lookaheadSymbols;
    }

//    boolean isSameTo(Item item) {
//        return (productionIndex == item.productionIndex
//                && position == item.position
//                && lookaheadSymbols.equals(item.lookaheadSymbols));
//    }

    @Override
    public int compareTo(Item o) {
        if (productionIndex < o.productionIndex) {
            return -1;
        } else if (productionIndex > o.productionIndex) {
            return 1;
        } else if (position < o.position) {
            return -1;
        } else if (position > o.position) {
            return 1;
        } else if (lookaheadSymbols.size() < o.lookaheadSymbols.size()) {
            return -1;
        } else if (lookaheadSymbols.size() > o.lookaheadSymbols.size()) {
            return 1;
        } else {
            Iterator<Integer> il = lookaheadSymbols.iterator(), ir = o.lookaheadSymbols.iterator();
            while (il.hasNext()) {
                Integer l = il.next(), r = ir.next();
                if (l < r) {
                    return -1;
                } else if (l > r) {
                    return 1;
                }
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return productionIndex == item.productionIndex
                && position == item.position;
    }

    @Override
    public int hashCode() {
        int result = productionIndex;
        result = 31 * result + position;
        result = 31 * result + lookaheadSymbols.hashCode();
        return result;
    }
}
