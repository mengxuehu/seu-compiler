package lex.fa;

import java.util.*;
import java.util.List;
import lex.fa.node.DfaNode;
import lex.fa.node.FaNode;
import lex.fa.node.NfaNode;

public class Dfa {

    private ArrayList<NfaNode> nfa;
    private ArrayList<DfaNode> nodes;
    private ArrayList<DfaNode> tempNodes;

    private int accStart;
    private int accNum;

    public int getAccStart() {
        return accStart;
    }

    public int getAccNum() {
        return accNum;
    }

    Dfa(ArrayList<NfaNode> nfa) {
        this.nfa = nfa;
        this.nodes = new ArrayList<>();
        this.tempNodes = new ArrayList<>();
        this.accNum = 0;
        this.accStart = 0;
    }

    private void changeIndex(DfaNode node,int index) {
        int index0 = node.getIndex();
        Map<String,Integer> trans;
        node.setIndex(index);
        for (DfaNode n : nodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if(trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge,index);
                }
            }
        }
        for (DfaNode n : tempNodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if(trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge,index);
                }
            }
        }
    }

    private Set<FaNode<Set<Integer>>> epsilonClosure(Set<FaNode<Set<Integer>>> set) {
        Deque<FaNode<Set<Integer>>> stack = new LinkedList<>();
        stack.addAll(set);
        Set<FaNode<Set<Integer>>> result = set;
        FaNode<Set<Integer>> t;
        Set<Integer> targets;
        while(!stack.isEmpty()) {
            t = stack.pop();
            targets = t.getTransition("");
            if(targets != null) {
                for (Integer i : targets) {
                    t = nfa.get(i);
                    if(!result.contains(t))
                        result.add(t);
                    stack.push(t);
                }
            }
        }
        return result;
    }

    private Set<FaNode<Set<Integer>>> move(String edge, Set<FaNode<Set<Integer>>> set) {
        Deque<FaNode<Set<Integer>>> stack = new LinkedList<>();
        stack.addAll(set);
        Set<FaNode<Set<Integer>>> result = new HashSet<>();
        FaNode<Set<Integer>> t;
        Set<Integer> targets;
        while(!stack.isEmpty()) {
            t = stack.pop();
            targets = t.getTransition(edge);
            if(targets != null) {
                for (Integer i : targets) {
                    t = nfa.get(i);
                    result.add(t);
                }
            }
        }
        return epsilonClosure(result);
    }

    private DfaNode toDfaNode(Set<FaNode<Set<Integer>>> set) {
        int action = 9999;
        Map<String, Set<Integer>> trans;
        DfaNode dfaNode = new DfaNode(0);
        for (FaNode<Set<Integer>> node : set) {
            if (node.isAccepting()) {
                if(node.getAction() < action) {
                    action = node.getAction();
                }
            }
        }
        if(action != 9999)
            dfaNode.setAccepting(action);

        return dfaNode;
    }

    ArrayList<DfaNode> nfaToDfa() {
        List<Set<FaNode<Set<Integer>>>> nodeSets = new ArrayList<>();
        List<Set<FaNode<Set<Integer>>>> nodeSetsNAcc = new ArrayList<>();
        List<Set<FaNode<Set<Integer>>>> nodeSetsAcc = new ArrayList<>();
        DfaNode tempNode = new DfaNode(0);
        Set<String> edges = new HashSet<String>();
        Set<FaNode<Set<Integer>>> set = new HashSet<>();
        Map<String, Set<Integer>> trans;
        int action,indexNAcc=0,indexAcc=-1,currNAcc=0,currAcc=0;
        boolean isAcc;

        Set<FaNode<Set<Integer>>> setTemp = new HashSet<>();
        set.add(nfa.get(0));
        tempNode = toDfaNode(epsilonClosure(set));

        if(tempNode.isAccepting()){
            tempNode.setIndex(indexAcc);
            indexAcc--;
            tempNodes.add(tempNode);
        } else {
            tempNode.setIndex(indexNAcc);
            indexNAcc++;
            nodes.add(tempNode);
        }

        while(currNAcc < nodeSetsNAcc.size() || currAcc < nodeSetsAcc.size()) {
            if(currNAcc < nodeSetsNAcc.size())
                isAcc = false;
            else isAcc = true;

            if(!isAcc) {
                set = nodeSetsNAcc.get(currNAcc);
                currNAcc++;
            } else {
                set = nodeSetsAcc.get(currAcc);
                currAcc++;
            }

                if (tempNode.isAccepting()) {
                    tempNode.setIndex(indexAcc);
                    indexAcc--;
                    tempNodes.add(tempNode);
                }

                for (FaNode<Set<Integer>> node : set) {
                    trans = node.getAllTransitions();
                    for (String edge : trans.keySet()) {
                        edges.add(edge);
                    }
                    }

                for (String edge : edges) {
                    if (!edge.equals("")) {
                        setTemp = move(edge, set);
                        tempNode = toDfaNode(setTemp);
                        if(!tempNode.isAccepting()) {
                            nodeSetsNAcc.add(setTemp);
                            nodes.get(currNAcc).addTransition(edge, nodeSets.indexOf(set));
                        } else {
                            nodeSetsAcc.add(setTemp);
                        }

                    }
                }
        }

        //adjust sequence
//        int i = -1;
//        for (int j =0 ;j < nodes.size();j++) {
//            if(nodes.get(j).isAccepting()) {
//                changeIndex(nodes.get(j),i);
//                tempNodes.add(nodes.get(j));
//                nodes.remove(nodes.get(j));
//                j--;
//                i--;
//            }
//        }
//        accStart = nodes.size();
//        nodes.addAll(tempNodes);
//        accNum = nodes.size() - accStart;
//        for (int j = 0;j < nodes.size();j++) {
//            if(nodes.get(j).getIndex() != j) {
//                changeIndex(nodes.get(j),j);
//            }
//        }

        //test
//        for(int j = 0;j < dfa.size();j++) {
//            Map<String,Integer> ts = dfa.get(j).getAllTransitions();
//            for (String edge : ts.keySet()) {
//                System.out.println(j + " " + edge + " " + ts.get(edge));
//            }
//        }

        return nodes;
    }

}