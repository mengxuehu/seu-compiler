package yacc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sxk on 06/05/17.
 */
public class LALR1 {

    private Set<ItemSet> itemSetOfLR1;
    private Set<UnionItemSet> itemSetOfLALR1 = new HashSet<>();

    LALR1(Set<ItemSet> itemSets) {
        this.itemSetOfLR1 = itemSets;
    }

    class UnionItemSet {
        private int state;
        private ArrayList<ItemSet> unionItemSet = new ArrayList<>();
        private ArrayList<Set<Integer>> lookaheadSymbols = new ArrayList<>();

        public ItemSet getFirstItem() {
            return unionItemSet.get(0);
        }

        public ArrayList<Set<Integer>> getLookaheadSymbols() {
            return lookaheadSymbols;
        }

        public void addItemSet(ItemSet state) {
            unionItemSet.add(state);
            if (lookaheadSymbols.isEmpty()){
                for (Item item : state.getItems()) {
                    lookaheadSymbols.add(item.getLookaheadSymbols());
                }
            } else {
                int i = 0;
                for (Item item : state.getItems()) {
                    for (Integer integerSymbols : item.getLookaheadSymbols()) {
                        if (!lookaheadSymbols.get(i).contains(integerSymbols)){
                            lookaheadSymbols.get(i).add(integerSymbols);
                        }
                    }

                }
            }
        }

        public boolean findItemSet(ItemSet state) {
            for (ItemSet stateOfItemSet : unionItemSet) {
                if (state.getState() == stateOfItemSet.getState()) {
                    return true;
                }
            }
            return false;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    public void generateLALR1() {

        //合并项集
        boolean ifAdd = false;
        int state = 0;
        for (ItemSet itemSet : itemSetOfLR1) {
            ifAdd = false;
            for (UnionItemSet unionItemSet : itemSetOfLALR1) {
                if (itemSet.equalItemSet(unionItemSet.getFirstItem())) {
                    unionItemSet.addItemSet(itemSet);
                    ifAdd = true;
                }
            }
            if (!ifAdd) {
                UnionItemSet unionSet = new UnionItemSet();
                unionSet.addItemSet(itemSet);
                itemSetOfLALR1.add(unionSet);
            }
        }

        //添加向前看符号
        for (UnionItemSet unionItemSet : itemSetOfLALR1) {

        }
    }
}
