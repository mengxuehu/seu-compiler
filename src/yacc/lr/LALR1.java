package yacc.lr;

import javafx.util.Pair;

import java.util.*;

/**
 * Created by sxk on 06/05/17.
 */
public class LALR1 {

    private Set<UnionItemSet> itemSetOfLALR1 = new HashSet<>();
    private Map<Pair<Integer, Integer>, Integer> tableGoto = new HashMap<>();
    private Map<Pair<Integer, Integer>, Action> tableAction = new HashMap<>();


    LALR1() {

    }

    public Map<Pair<Integer, Integer>, Integer> getTableGoto() {
        return tableGoto;
    }

    public void setTableGoto(Map<Pair<Integer, Integer>, Integer> tableGoto) {
        this.tableGoto = tableGoto;
    }

    public Map<Pair<Integer, Integer>, Action> getTableAction() {
        return tableAction;
    }

    public void setTableAction(Map<Pair<Integer, Integer>, Action> tableAction) {
        this.tableAction = tableAction;
    }

    public boolean generateLALR1(Set<ItemSet> itemSetOfLR1, Map<Pair<Integer, Integer>, Integer> tableGoto, Map<Pair<Integer, Integer>, Action> tableAction) {
        this.tableAction = tableAction;
        this.tableGoto = tableGoto;
        unionItemSet(itemSetOfLR1);
//        System.out.println(itemSetOfLALR1.size());
        if (generateGotoTable() && generateActionTable())
            return true;
        else
            return false;
    }

    private void unionItemSet(Set<ItemSet> itemSetOfLR1) {

        //合并项集
        boolean ifAdd = true;
        int state = 0;
        for (ItemSet itemSet : itemSetOfLR1) {
            ifAdd = false;
            for (UnionItemSet unionItemSet : itemSetOfLALR1) {
                if (itemSet.equalItemSet(unionItemSet.getFirstItem())) {
//                    System.out.println("new");
//                    for (Item item : itemSet.getItems()) {
//                        System.out.println(item.getProductionIndex());
//                    }
//                    System.out.println("old");
//                    for (Item item : unionItemSet.getFirstItem().getItems()) {
//                        System.out.println(item.getProductionIndex());
//                    }
                    unionItemSet.addItemSet(itemSet);
                    ifAdd = true;
                }
            }
            if (!ifAdd) {
                UnionItemSet unionSet = new UnionItemSet();
                unionSet.addItemSet(itemSet);
                unionSet.setState(state++);
                itemSetOfLALR1.add(unionSet);
            }
        }
    }

    private boolean generateGotoTable() {
        Map<Pair<Integer, Integer>, Integer> newTableGoto = new HashMap<>();

        for (Pair<Integer, Integer> gotoPair : tableGoto.keySet()) {
            Integer gotoPairKey = 0;
            Integer gotoPairVal = gotoPair.getValue();
            Integer tableGotoValue = 0;
            boolean isFind = false;
            for (UnionItemSet unionItemSet : itemSetOfLALR1) {
                for (ItemSet itemSet : unionItemSet.getAllItemSet()) {
                    if (itemSet.getState() == tableGoto.get(gotoPair)) {
                        tableGotoValue = unionItemSet.getState();
                        isFind = true;
                        break;
                    }
                }
                if (isFind)
                    break;
            }
            isFind = false;
            for (UnionItemSet unionItemSet : itemSetOfLALR1) {
                for (ItemSet itemSet : unionItemSet.getAllItemSet()) {
                    if (itemSet.getState() == gotoPair.getKey()) {
                        gotoPairKey = unionItemSet.getState();
                        isFind = true;
                        break;
                    }
                }
                if (isFind)
                    break;
            }
            Pair<Integer, Integer> a = new Pair<>(gotoPairKey, gotoPairVal);
            if (newTableGoto.containsKey(a) &&
                    !newTableGoto.get(a).equals(tableGotoValue)) {
//                System.out.println(newTableGoto.get(new Pair<>(gotoPairKey, gotoPairVal)));
//                System.out.println(tableGotoValue);
//                System.err.println("error LALR1");
                return false;
            }
            newTableGoto.put(new Pair<>(gotoPairKey, gotoPairVal), tableGotoValue);
        }
        tableGoto = newTableGoto;
        return true;
    }

    private boolean generateActionTable() {
        Map<Pair<Integer, Integer>, Action> newTableAction = new HashMap<>();
        for (Pair<Integer, Integer> actionPair : tableAction.keySet()) {
            Integer actionPairKey = -1;
            Integer actionPairVal = actionPair.getValue();
            for (UnionItemSet unionItemSet : itemSetOfLALR1) {
                for (ItemSet itemSet : unionItemSet.getAllItemSet()) {
                    if (itemSet.getState() == actionPair.getKey()) {
                        actionPairKey = unionItemSet.getState();
                        break;
                    }
                }
                if (actionPairKey != -1)
                    break;
            }
            Pair<Integer, Integer> action = new Pair<Integer, Integer>(actionPairKey, actionPairVal);
            Action old = tableAction.get(actionPair);
            Action now = newTableAction.get(action);
            if (newTableAction.keySet().contains(action)) {
                if (old.getType() == now.getType()) {
                    if (old.getType() == ActionType.SHIFT){
                        if (((ShiftAction) old).getShiftTarget() != ((ShiftAction) old).getShiftTarget())
                            return false;
                    } else if (old.getType() == ActionType.REDUCE) {
                        if (((ReduceAction) old).getProductionReducingBy() != ((ReduceAction) old).getProductionReducingBy())
                            return false;
                    }
                } else {
                    return false;
                }

            } else {
                newTableAction.put(action, tableAction.get(actionPair));
            }

        }
        tableAction = newTableAction;
        return true;
    }

    class UnionItemSet {
        private int state;
        private ArrayList<ItemSet> unionItemSet = new ArrayList<>();
        private ArrayList<Set<Integer>> lookaheadSymbols = new ArrayList<>();

        public ItemSet getFirstItem() {
            return unionItemSet.get(0);
        }

        public ArrayList<ItemSet> getAllItemSet() {
            return unionItemSet;
        }

        public ArrayList<Set<Integer>> getLookaheadSymbols() {
            return lookaheadSymbols;
        }

        public void addItemSet(ItemSet state) {
            unionItemSet.add(state);
            if (lookaheadSymbols.isEmpty()) {
                for (Item item : state.getItems()) {
                    lookaheadSymbols.add(item.getLookaheadSymbols());
                }
            } else {
                int i = 0;
                for (Item item : state.getItems()) {
                    for (Integer integerSymbols : item.getLookaheadSymbols()) {
                        lookaheadSymbols.get(i).add(integerSymbols);
                    }
                    i++;
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

}
