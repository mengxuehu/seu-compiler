package yacc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sxk on 06/05/17.
 */
public class LALR1 {

    private ArrayList<UnionItemSet> itemSetOfLALR1 = new ArrayList<>();

    class UnionItemSet {
        private ArrayList<Integer> unionItemSet = new ArrayList<>();
        private Set<String> lookaheadSymbols = new HashSet<>();

        public void addItemSet(Integer state) {
            unionItemSet.add(state);
        }

        public void addLookaheadSymbols(String symbol) {
            lookaheadSymbols.add(symbol);
        }

        public boolean findItemSet(Integer state) {
            for (Integer stateOfItemSet : unionItemSet) {
                if (state == stateOfItemSet) {
                    return true;
                }
            }
            return false;
        }
    }

    public void generateLALR1(){

    }
}
