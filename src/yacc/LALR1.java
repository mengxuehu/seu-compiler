package yacc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sxk on 06/05/17.
 */
public class LALR1 {

    private Set<LR1.ItemSet> itemSetOfLR1;
    private Set<UnionItemSet> itemSetOfLALR1 = new HashSet<>();

    LALR1(Set<LR1.ItemSet> itemSets) {
        this.itemSetOfLR1 = itemSets;
    }

    class UnionItemSet {
        private ArrayList<LR1.ItemSet> unionItemSet = new ArrayList<>();
        private Set<String> lookaheadSymbols = new HashSet<>();

        public LR1.ItemSet getFirstItem() {
            return unionItemSet.get(0);
        }

        public Set<String> getLookaheadSymbols() {
            return lookaheadSymbols;
        }

        public void addItemSet(LR1.ItemSet state) {
            unionItemSet.add(state);
        }

        public void addLookaheadSymbols(String symbol) {
            lookaheadSymbols.add(symbol);
        }

        public boolean findItemSet(LR1.ItemSet state) {
            for (LR1.ItemSet stateOfItemSet : unionItemSet) {
                if (state.getState() == stateOfItemSet.getState()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void generateLALR1() {

        //合并项集
        boolean ifAdd = false;
        for (LR1.ItemSet itemSet : itemSetOfLR1) {
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
