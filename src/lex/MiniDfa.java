package lex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

class MiniDfa {

    private ArrayList<FaNode<Integer>> dfa;
    private int acceptingStartState = 0;
    private int numOfAccepting = 0;

    class NonTerminalSet {
        int state;
        HashSet<Integer> members = new HashSet<>();
        HashSet<Integer> destination = new HashSet<>();

        public void addMember(Integer mem) {
            members.add(mem);
        }

        public void addAllMembers(HashSet<Integer> mem) {
            this.members = mem;
        }

        public void addDestination(Integer des) {
            destination.add(des);
        }

        public void addAllDestination(HashSet<Integer> des) {
            this.destination = des;
        }

        public boolean findMembers(Integer mem) {
            return members.contains(mem);
        }

        public HashSet<Integer> getMembers() {
            return members;
        }

        public void setState(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }

        public boolean findDestination(Integer des) {
            return destination.contains(des);
        }

        public boolean tellDestination(HashSet<Integer> des) {
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

    }

    private void miniDfa() {
        LinkedList<NonTerminalSet> oldSet = new LinkedList<>();
        LinkedList<NonTerminalSet> newSet = new LinkedList<>();
        NonTerminalSet initSet = new NonTerminalSet();
        int i = 0, j = 0, k = 0;
        Integer state = 0;
        boolean isChanged = true;
        for (FaNode<Integer> dfaNode : dfa) {
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
                            newNonTerminalSet.addMember(state);
                            newNonTerminalSet.addAllDestination(deSet);
                            i++;
                        }

                    }
                }
            }
            if (newSet.size() == oldSet.size()) {
                isChanged = false;
            } else {
                oldSet = newSet;
                newSet.clear();
            }
        }

		/*创建最小化DFA*/
        i = 0;
        ArrayList<FaNode<Integer>> miniDfa = new ArrayList<>();
        for (NonTerminalSet set1 : newSet) {
            j = 0;
            state = set1.getMembers().iterator().next();
            FaNode<Integer> newDfaNode = new FaNode<Integer>(i) {

                @Override
                void addTransition(String edge, int target) {
                    // TODO Auto-generated method stub

                }

                @Override
                void addAllTransitions(Map<String, Integer> transitions) {
                    // TODO Auto-generated method stub

                }
            };
            for (NonTerminalSet set2 : newSet) {
                /*
                 * 查找set1中有没有相应目的地，如果有就遍历set2的members
				 * 找到state到set2的members中所有的边
				 */
                if (set1.findDestination(set2.getState())) {
                    for (Integer member : set2.getMembers()) {
                        Map<String, Integer> allDes = dfa.get(state).getAllTransitions();
                        for (Map.Entry<String, Integer> entry : allDes.entrySet()) {
                            if (entry.getValue() == member) {
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

        for (i = acceptingStartState; i < acceptingStartState + numOfAccepting; i++) {
            Map<String, Integer> allDes = dfa.get(i).getAllTransitions();
            FaNode<Integer> newDfaNode = new FaNode<Integer>(j) {

                @Override
                void addTransition(String edge, int target) {
                    // TODO Auto-generated method stub

                }

                @Override
                void addAllTransitions(Map<String, Integer> transitions) {
                    // TODO Auto-generated method stub

                }
            };
            for (Map.Entry<String, Integer> entry : allDes.entrySet()) {
                for (NonTerminalSet set : newSet) {
                    if (set.findMembers(i)) {
                        newDfaNode.addTransition(entry.getKey(), i);
                    }
                }
            }
            miniDfa.add(newDfaNode);
            j++;
        }

        dfa = miniDfa;
    }
}
