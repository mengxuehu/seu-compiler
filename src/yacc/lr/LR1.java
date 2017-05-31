package yacc.lr;


import javafx.util.Pair;
import yacc.entity.*;

import java.util.*;

class LR1 {
    private Productions productions;
    private Symbols symbols;
    private Map<Integer, HashSet<Integer>> firsts;
    private Set<ItemSet> collection;
    private Map<Pair<Integer, Integer>, Integer> tableGoto;
    private Map<Pair<Integer, Integer>, Action> tableAction;
    private Map<Integer, Precedence> precedence;
    private Map<Integer, Associativity> associativity;

    LR1() {
    }

    void parse(Productions productions, Symbols symbols,
               Map<Integer, Precedence> precedence, Map<Integer, Associativity> associativity) {
        this.productions = productions;
        this.symbols = symbols;
        this.precedence = precedence;
        this.associativity = associativity;

        firsts = new HashMap<>();
        tableGoto = new HashMap<>();
        tableAction = new HashMap<>();

        constructFirsts();
        constructCollection();
        constructAction();
    }

    private void constructFirsts() {
        for (Integer terminal : symbols.getTerminalIndexes()) {
            multiMapPut(firsts, terminal, terminal);
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
                if (prodsBeginWith.containsKey(entry.getKey())) {
                    for (Integer head : prodsBeginWith.get(entry.getKey())) {
                        for (Integer first : entry.getValue()) {
                            Set tmp = firsts.get(head);
                            if (tmp == null || !tmp.contains(first)) {
                                multiMapPut(firsts, head, first);
                                multiMapPut(tmpChanged, head, first);
                            }
                        }
                    }
                }
            }
            changed = tmpChanged;
            tmpChanged = new HashMap<>();
        }
    }

    private void constructCollection() {
        Map<ItemSet, Integer> tmpCollectionMap = new HashMap<>();
        ItemSet initItemSet = new ItemSet(0);
        Item item = new Item(productions.getStart().getIndex(), 0);
        item.addLookaheadSymbol(symbols.getEnd());
        initItemSet.addItem(item);
        initItemSet.closure(productions, symbols, firsts);
        tmpCollectionMap.put(initItemSet, initItemSet.getState());

        ArrayList<ItemSet> tmpCollectionList = new ArrayList<>(tmpCollectionMap.keySet());
        for (int i = 0; i < tmpCollectionList.size(); i++) {
            ItemSet currentItemSet = tmpCollectionList.get(i);
            int currentState = currentItemSet.getState();
            Set<Integer> nextSymbols = currentItemSet.getNextSymbols(productions, symbols);

            for (Integer j : nextSymbols) {
                if (symbols.getEnd() != j) {
                    ItemSet nextItemSet = currentItemSet.goto_(j, productions, symbols, firsts);
                    if (nextItemSet == null) {
                        continue;
                    }

                    Integer nextState = tmpCollectionMap.get(nextItemSet);
                    if (nextState == null) {
                        nextState = tmpCollectionList.size();
                        nextItemSet.setState(nextState);
                        tmpCollectionMap.put(nextItemSet, nextState);
                        tmpCollectionList.add(nextItemSet);
                    }
                    if (symbols.isTerminal(j)) {
                        tableAction.put(new Pair<>(currentState, j), new ShiftAction(nextState));
                    } else {
                        tableGoto.put(new Pair<>(currentState, j), nextState);
                    }
                }
            }
        }

        collection = tmpCollectionMap.keySet();
    }

    private void constructAction() {
        for (ItemSet itemSet : collection) {
            for (Item item : itemSet.getItems()) {
                Production production = productions.getProduction(item.getProductionIndex());

                // reduce or accept
                if (item.getPosition() == production.getBody().size()) {
                    if (production.getHead() == symbols.getStartAug()
                            && item.getLookaheadSymbols().contains(symbols.getEnd())) {
                        Pair<Integer, Integer> key = new Pair<>(itemSet.getState(), symbols.getEnd());
                        addNewAction(key, new AcceptAction());
                    } else {
                        Action reduceAction = new ReduceAction(item.getProductionIndex());
                        for (Integer lookahead : item.getLookaheadSymbols()) {
                            Pair<Integer, Integer> key = new Pair<>(itemSet.getState(), lookahead);
                            addNewAction(key, reduceAction);
                        }
                    }
                }
            }
        }
    }

    private <K, V> void multiMapPut(Map<K, HashSet<V>> multiMap, K key, V value) {
        multiMap.computeIfAbsent(key, k -> new HashSet<>());
        multiMap.get(key).add(value);
    }

    private void addNewAction(Pair<Integer, Integer> key, Action action) {
        Action oldAction = tableAction.get(key);
        if (oldAction != null) {
            // TODO: 处理冲突
            if(oldAction.getType() == ActionType.REDUCE && action.getType() == ActionType.SHIFT ||
                    oldAction.getType() == ActionType.SHIFT && action.getType() == ActionType.REDUCE) {
                ReduceAction reduceAction = new ReduceAction(0);
                ShiftAction shiftAction = new ShiftAction(0);
                if(oldAction.getType() == ActionType.REDUCE) {
                    reduceAction = (ReduceAction) oldAction;
                    shiftAction = (ShiftAction) action;
                }
                else {
                    reduceAction = (ReduceAction) action;
                    shiftAction = (ShiftAction) oldAction;
                }
                if(precedence.get(key.getValue()) != null && getProductionPrecedence(reduceAction.getProductionReducingBy()) != null) {
                    int compare = getProductionPrecedence(reduceAction.getProductionReducingBy()).compareTo(precedence.get(key.getValue()));
                    switch (compare) {
                        case -1:
                            tableAction.replace(key, shiftAction);
                            break;
                        case 0:
                            if(associativity.get(key.getValue()) != null) {
                            if (associativity.get(key.getValue()).isLeftAssociativity())
                                tableAction.replace(key, reduceAction);
                            else
                                tableAction.replace(key, shiftAction);
                            } else {
                                tableAction.replace(key, shiftAction);
                                System.err.println("WARNING: shift/reduce conflict, shift will be taken");
                            }
                        case 1:
                            tableAction.replace(key, reduceAction);
                            break;
                        default:
                            break;
                    }
                }
                else {
                    tableAction.replace(key, shiftAction);
                    System.err.println("WARNING: shift/reduce conflict, shift will be taken");
                }
            }
            else {
                ReduceAction oldReduceAction = (ReduceAction) oldAction;
                ReduceAction reduceAction = (ReduceAction) action;
                if(oldReduceAction.getProductionReducingBy() < reduceAction.getProductionReducingBy())
                    tableAction.replace(key, oldReduceAction);
                else tableAction.replace(key, reduceAction);
                System.err.println("WARNING: reduce/reduce conflict, the former reduce will be taken");
            }
        } else {
            tableAction.put(key, action);
        }
    }

    private Precedence getProductionPrecedence(int index) {
        Production production = productions.getProduction(index);
        List<Integer> body = production.getBody();
        for(int i = body.size();i > 0;i--) {
            if(symbols.getTerminalIndexes().contains(body.get(i-1)))
                return precedence.get(body.get(i-1));
        }
        return null;
    }

    Set<ItemSet> getCollection() {
        return collection;
    }

    Map<Pair<Integer, Integer>, Integer> getTableGoto() {
//        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : tableGoto.entrySet()) {
//            if (entry.getValue() == null) {
//                System.out.println("error");
//                System.exit(10);
//            }
//        }
        return tableGoto;
    }

    Map<Pair<Integer, Integer>, Action> getTableAction() {
        return tableAction;
    }
}
