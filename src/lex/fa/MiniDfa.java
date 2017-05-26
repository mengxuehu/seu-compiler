package lex.fa;

import lex.fa.node.DfaNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

class MiniDfa {


    private ArrayList<DfaNode> dfa;
    private int acceptingStartState = 0;

    MiniDfa() {
    }

    ArrayList<DfaNode> getDfa() {
        return dfa;
    }

    void miniDfa(ArrayList<DfaNode> dfa, int accstart, int numOfAccepting) {
        this.dfa = dfa;
        acceptingStartState = accstart;
        //get divide set
        LinkedList<NonTerminalSet> newSet = divideDfa();

		/*创建最小化DFA*/
        int i = 0, j, state;
        int numNonAccept = newSet.size();
        ArrayList<DfaNode> miniDfa = new ArrayList<>();
        //join non-accept state
        for (NonTerminalSet set1 : newSet) {
            j = 0;
            state = set1.getMembers().iterator().next();
            Map<String, Integer> allDes = dfa.get(state).getAllTransitions();
            DfaNode newDfaNode = new DfaNode(i);

            //solve accept
            for (Integer integer : set1.getAccept()) {
                for (Map.Entry<String, Integer> entry : allDes.entrySet()) {
                    if (entry.getValue().equals(integer)) {
                        newDfaNode.addTransition(entry.getKey(), integer - numNonAccept);
                    }
                }
            }
            //solve nonaccept
            for (NonTerminalSet set2 : newSet) {
                /*
                 * 查找set1中有没有相应目的地，如果有就遍历set2的members
				 * 找到state到set2的members中所有的边
				 */
                if (set1.findDestination(set2.getState())) {
                    for (Integer member : set2.getMembers()) {
                        for (Map.Entry<String, Integer> entry : allDes.entrySet()) {
                            if (entry.getValue().equals(member)) {
                                newDfaNode.addTransition(entry.getKey(), j);
                            }
                        }
                    }
                }
                j++;
            }
            miniDfa.add(newDfaNode);
            i++;
        }
        //join accept state
        for (j = acceptingStartState; j < acceptingStartState + numOfAccepting; j++) {
            Map<String, Integer> allDes = dfa.get(j).getAllTransitions();
            DfaNode newDfaNode = new DfaNode(i);
            newDfaNode.setAccepting(dfa.get(j).getAction());
            for (Map.Entry<String, Integer> entry : allDes.entrySet()) {
                for (NonTerminalSet set : newSet) {
                    if (set.findMembers(j)) {
                        newDfaNode.addTransition(entry.getKey(), i);
                    }
                }
            }
            miniDfa.add(newDfaNode);
            i++;
        }

        this.dfa = miniDfa;
    }

    private LinkedList<NonTerminalSet> divideDfa() {
        LinkedList<NonTerminalSet> oldSet = new LinkedList<>();
        LinkedList<NonTerminalSet> newSet = new LinkedList<>();
        NonTerminalSet initSet = new NonTerminalSet();
        int i;
        Integer state = 0;
        boolean isChanged = true;
        for (DfaNode dfaNode : dfa) {
            if (!dfaNode.isAccepting()) {
                initSet.addMember(dfaNode.getIndex());
            }
        }
        initSet.setState(0);
        oldSet.add(initSet);

        while (isChanged) {
            i = 0;
            for (NonTerminalSet nonTerminalSet : oldSet) {
                HashSet<Integer> setMember = nonTerminalSet.getMembers();
                for (Integer memStates : setMember) {
                    NonTerminalSet newNonTerminalSet = new NonTerminalSet();
                    Map<String, Integer> trans = dfa.get(memStates).getAllTransitions();
                    HashSet<Integer> deSet = new HashSet<>();
                    boolean isAdd = false;
                    if (newSet.isEmpty()) {
                        /*
                         * 遍历dfa的state，记录到达的目的地，加入到addDes()中
						 *
						 */
                        newNonTerminalSet.setState(i);
                        for (Integer des : trans.values()) {
                            if (des >= acceptingStartState) {
                                newNonTerminalSet.addDestination(des);
                            } else {
                                for (NonTerminalSet oldNTSet : oldSet) {
                                    if (oldNTSet.findMembers(des)) {
                                        newNonTerminalSet.addDestination(oldNTSet.getState());
                                    }
                                }
                            }
                        }
                        newNonTerminalSet.addMember(memStates);
                        newSet.add(newNonTerminalSet);
                        i++;
                    } else {
                        /*
                         * 使用HashSet<>记录目的地
						 * 查找是否有目的地相同的state，如果有直接addMembers()
						 * 遍历dfa的state，记录到达的目的地，加入到addDes()中
						 *
						 */
                        for (Integer des : trans.values()) {
                            if (des >= acceptingStartState) {
                                deSet.add(des);
                            } else {
                                for (NonTerminalSet oldNTSet : oldSet) {
                                    if (oldNTSet.findMembers(des)) {
                                        deSet.add(oldNTSet.getState());
                                    }
                                }
                            }
                        }
                        for (NonTerminalSet newNTSet : newSet) {
                            if (newNTSet.tellDestination(deSet)) {
                                newNTSet.addMember(memStates);
                                isAdd = true;
                                break;
                            }
                        }
                        if (!isAdd) {
                            newNonTerminalSet.setState(i);
                            newNonTerminalSet.addMember(memStates);
                            newNonTerminalSet.addAllDestination(deSet);
                            newSet.add(newNonTerminalSet);
                            i++;
                        }

                    }
                }
            }
            if (newSet.size() == oldSet.size()) {
                isChanged = false;
            } else {
                oldSet = newSet;
                newSet = new LinkedList<>();
            }
        }
        return newSet;
    }

    class NonTerminalSet {
        int state;
        HashSet<Integer> members = new HashSet<>();
        HashSet<Integer> destination = new HashSet<>();

        void addMember(Integer mem) {
            members.add(mem);
        }

        void addDestination(Integer des) {
            destination.add(des);
        }

        void addAllDestination(HashSet<Integer> des) {
            this.destination = des;
        }

        boolean findMembers(Integer mem) {
            return members.contains(mem);
        }

        HashSet<Integer> getMembers() {
            return members;
        }

        int getState() {
            return state;
        }

        void setState(int state) {
            this.state = state;
        }

        boolean findDestination(Integer des) {
            return destination.contains(des);
        }

        boolean tellDestination(HashSet<Integer> des) {
            if (des.size() != destination.size()) {
                return false;
            } else {
                for (Integer object : des) {
                    if (!destination.contains(object)) {
                        return false;
                    }
                }
            }
            return true;
        }

        HashSet<Integer> getAccept() {
            HashSet<Integer> accept = new HashSet<>();
            for (Integer integer : destination) {
                if (integer >= acceptingStartState)
                    accept.add(integer);
            }
            return accept;
        }

    }
}
