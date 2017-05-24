package lex.fa;

import lex.fa.node.DfaNode;
import lex.fa.node.FaNode;

import java.util.*;

class Dfa {
    private ArrayList<FaNode<Set<Integer>>> nfa;
    private LinkedList<DfaNode> nodes;
    private LinkedList<DfaNode> tempNodes;
    private int accStart;
    private int accNum;

    Dfa(ArrayList<FaNode<Set<Integer>>> nfa) {
        this.nfa = nfa;
        this.nodes = new LinkedList<>();
        this.tempNodes = new LinkedList<>();
        this.accNum = 0;
        this.accStart = 0;
    }

    int getAccStart() {
        return accStart;
    }

    int getAccNum() {
        return accNum;
    }

    ArrayList<DfaNode> nfaToDfa() {
        List<Set<FaNode<Set<Integer>>>> nodeSets = new LinkedList<>();
        HashSet<String> edges = new HashSet<String>();
        Map<String, Set<Integer>> trans;
        int action;

        Set<FaNode<Set<Integer>>> set = new HashSet<>();
        set.add(nfa.get(0));
        nodeSets.add(epsilonClosure(set));

        for (int i = 0; i < nodeSets.size(); i++) {
            nodes.add(new DfaNode(i));
            edges.clear();
            action = 9999;

            for (FaNode<Set<Integer>> node : nodeSets.get(i)) {
                trans = node.getAllTransitions();
                for (String edge : trans.keySet()) {
                    edges.add(edge);
                }
                if (node.isAccepting()) {
                    if (node.getAction() < action) {
                        action = node.getAction();
                        nodes.get(i).setAccepting(action);
                    }
                }
            }

            for (String edge : edges) {
                set = move(edge, nodeSets.get(i));
                if (set.isEmpty())
                    break;
                if (!nodeSets.contains(set)) {
                    nodeSets.add(set);
                    nodes.get(i).addTransition(edge, (nodeSets.size() - 1));
                } else {
                    nodes.get(i).addTransition(edge, nodeSets.indexOf(set));
                }
            }
        }

        //adjust sequence
        int i = -1;
        for (int j = 0; j < nodes.size(); j++) {
            if (nodes.get(j).isAccepting()) {
                changeIndex(nodes.get(j), i);
                tempNodes.add(nodes.get(j));
                nodes.remove(nodes.get(j));
                i--;
            }
        }
        accStart = nodes.size();
        nodes.addAll(tempNodes);
        accNum = nodes.size() - accStart;
        for (int j = 0; j < nodes.size(); j++) {
            if (nodes.get(j).getIndex() != j) {
                changeIndex(nodes.get(j), j);
            }
        }

        ArrayList<DfaNode> dfa = new ArrayList<>(nodes.size());
        for (int j = 0; j < nodes.size(); j++) {
            dfa.add(null);
        }
        for (DfaNode node : nodes) {
            if (dfa.get(node.getIndex()) == null) {
                dfa.set(node.getIndex(), node);
            } else {
                System.err.println("error");
            }
        }

        return dfa;
    }

    private Set<FaNode<Set<Integer>>> epsilonClosure(Set<FaNode<Set<Integer>>> set) {
        Deque<FaNode<Set<Integer>>> stack = new LinkedList<>();
        stack.addAll(set);
        Set<FaNode<Set<Integer>>> result = set;
        FaNode<Set<Integer>> t;
        Set<Integer> targets;
        while (!stack.isEmpty()) {
            t = stack.pop();
            targets = t.getTransition("");
            if (targets != null) {
                for (Integer i : targets) {
                    t = nfa.get(i);
                    if (!result.contains(t))
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
        while (!stack.isEmpty()) {
            t = stack.pop();
            targets = t.getTransition(edge);
            if (targets != null) {
                for (Integer i : targets) {
                    t = nfa.get(i);
                    if (!result.contains(t))
                        result.add(t);
                }
            }
        }
        return epsilonClosure(result);
    }

    private void changeIndex(DfaNode node, int index) {
        int index0 = node.getIndex();
        Map<String, Integer> trans;
        for (DfaNode n : nodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if (trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge, index);
                }
            }
        }
        for (DfaNode n : tempNodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if (trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge, index);
                }
            }
        }
    }

}