package yacc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.util.Pair;

/**
 * Created by sxk on 06/05/17.
 */
public class LALR1 {

    private Set<UnionItemSet> itemSetOfLALR1 = new HashSet<>();
    private Map<Pair<Integer, Integer>, Integer> tableGoto = new HashMap<>();
    private Map<Pair<Integer, Integer>, Action> tableAction = new HashMap<>();
    
    
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

	
    
    LALR1() {
    	
    }
    
    public boolean generateLALR1(Set<ItemSet> itemSetOfLR1) {
		unionItemSet(itemSetOfLR1);
		generateTable(itemSetOfLR1);
		return false;
	}
    
    private boolean generateTable(Set<ItemSet> itemSetOfLR1) {
    	Map<Pair<Integer, Integer>, Integer> newTableGoto = new HashMap<>();
        Map<Pair<Integer, Integer>, Action> newTableAction = new HashMap<>();
        
    	for (Pair<Integer, Integer> gotoPair : tableGoto.keySet()) {
    		Integer gotoPairKey = 0;
			Integer gotoPairVal = 0;
    		for (UnionItemSet unionItemSet : itemSetOfLALR1) {
				for (ItemSet itemSet : unionItemSet.getAllItemSet()) {
					if (itemSet.getState() == gotoPair.getKey()) {
						gotoPairKey = unionItemSet.getState();
						break;
					}
				}
				gotoPairVal = gotoPair.getValue();
				newTableGoto.put(new Pair<Integer, Integer>(gotoPairKey, gotoPairVal), tableGoto.get(gotoPair));
			}
		}
    	
    	for (Pair<Integer, Integer> actionPair : tableAction.keySet()) {
    		Integer actionPairKey = 0;
			Integer actionPairVal = 0;
			boolean conflict = false;
    		for (UnionItemSet unionItemSet : itemSetOfLALR1) {
				for (ItemSet itemSet : unionItemSet.getAllItemSet()) {
					if (itemSet.getState() == actionPair.getKey()) {
						actionPairVal = unionItemSet.getState();
						break;
					}
				}
				actionPairVal = actionPair.getValue();
				Pair<Integer, Integer> action = new Pair<Integer, Integer>(actionPairKey, actionPairVal);
				for (Pair<Integer, Integer> exist : newTableAction.keySet()) {
					if (exist.equals(action)) {
						//冲突处理代码
						conflict = true;
					}
				}
				if (!conflict) {
					newTableGoto.put(action, tableGoto.get(actionPair));
				} else {
					//冲突解决添加或修改
				}
				
			}
		}
    	
		return false;
	}

    private void unionItemSet(Set<ItemSet> itemSetOfLR1) {

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
    
}
