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
        int action = Integer.MAX_VALUE;
        DfaNode dfaNode = new DfaNode(0);
        for (FaNode<Set<Integer>> node : set) {
            if (node.isAccepting()) {
                if(node.getAction() < action) {
                    action = node.getAction();
                }
            }
        }
        if(action != Integer.MAX_VALUE)
            dfaNode.setAccepting(action);

        return dfaNode;
    }

    ArrayList<DfaNode> nfaToDfa() {
        List<Set<FaNode<Set<Integer>>>> currNodeSets = new ArrayList<>();
        List<Set<FaNode<Set<Integer>>>> nodeSetsNAcc = new ArrayList<>();
        List<Set<FaNode<Set<Integer>>>> nodeSetsAcc = new ArrayList<>();
        DfaNode currNode = new DfaNode(0);
        DfaNode tempNode = new DfaNode(0);
        Set<FaNode<Set<Integer>>> currSet = new HashSet<>();
        Set<FaNode<Set<Integer>>> tempSet = new HashSet<>();
        Set<String> edges = new HashSet<String>();
        Map<String, Set<Integer>> trans;
        int currNAcc=0,currAcc=0,index;


        currSet.add(nfa.get(0));
        tempNode = toDfaNode(epsilonClosure(currSet));
        if(tempNode.isAccepting()){
            tempNodes.add(tempNode);
            tempNode.setIndex(-1-tempNodes.indexOf(tempNode));
            nodeSetsAcc.add(currSet);
        } else {
            nodes.add(tempNode);
            tempNode.setIndex(nodes.indexOf(tempNode));
            nodeSetsNAcc.add(currSet);
        }

        while(currNAcc < nodeSetsNAcc.size() || currAcc < nodeSetsAcc.size()) {
            if(currNAcc < nodeSetsNAcc.size()) {
                currNode = nodes.get(currNAcc);
                currNodeSets = nodeSetsNAcc;
                index = currNAcc;
                currNAcc++;
            }
            else  {
                currNode = tempNodes.get(currAcc);
                currNodeSets = nodeSetsAcc;
                index = currAcc;
                currAcc++;
            }

                currSet = currNodeSets.get(index);

                for (FaNode<Set<Integer>> node : currSet) {
                    trans = node.getAllTransitions();
                    for (String edge : trans.keySet()) {
                        edges.add(edge);
                    }
                }

                for (String edge : edges) {
                    if (!edge.equals("")) {
                        tempSet = move(edge, currSet);
                        tempNode = toDfaNode(tempSet);
                        if(!tempNode.isAccepting()) {
                            if(!nodeSetsNAcc.contains(tempSet)) {
                                nodeSetsNAcc.add(tempSet);
                                nodes.add(tempNode);
                                tempNode.setIndex(nodes.indexOf(tempNode));
                            }
                            currNode.addTransition(edge, nodeSetsNAcc.indexOf(tempSet));
                        } else {
                            if(!nodeSetsAcc.contains(tempSet)) {
                                nodeSetsAcc.add(tempSet);
                                tempNodes.add(tempNode);
                                tempNode.setIndex(-1-tempNodes.indexOf(tempNode));
                            }
                            currNode.addTransition(edge, -1-nodeSetsAcc.indexOf(tempSet));
                        }
                    }
                }
        }

        //adjust sequence
        accStart = nodes.size();
        nodes.addAll(tempNodes);
        accNum = nodes.size() - accStart;
        for (int j = accStart;j < nodes.size();j++) {
                changeIndex(nodes.get(j),j);
        }

        //test
//        for(int j = 0;j < nodes.size();j++) {
//            Map<String,Integer> ts = nodes.get(j).getAllTransitions();
//            for (String edge : ts.keySet()) {
//                System.out.println(j + " " + edge + " " + ts.get(edge));
//            }
//        }

        return nodes;
    }

}