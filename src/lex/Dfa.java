package lex;

import java.util.*;

public class Dfa {
    private class DfaNode extends FaNode<Integer> {
        DfaNode(int index) {
            super(index);
        }

        DfaNode(int index, String edge, int target) {
            super(index);
            this.addTransition(edge, target);
        }

        @Override
        void addTransition(String edge, int target) {
                this.transitions.put(edge, target);
        }

        @Override
        void addAllTransitions(Map<String, Integer> transitions) {
        }
    }

    private class NodeSet {
        List<FaNode<Set<Integer>>> nodes;

        NodeSet(List<FaNode<Set<Integer>>> nodes) {
            this.nodes = nodes;
        }
    }

    private ArrayList<FaNode<Set<Integer>>> nfa;
    private LinkedList<DfaNode> nodes;
    private LinkedList<DfaNode> tempNodes;

    public int getAccStart() {
        return accStart;
    }

    public int getAccNum() {
        return accNum;
    }

    private int accStart;
    private int accNum;

    Dfa(ArrayList<FaNode<Set<Integer>>> nfa) {
        this.nfa = nfa;
        this.nodes = new LinkedList<>();
        this.tempNodes = new LinkedList<>();
        this.accNum = 0;
        this.accStart = 0;
    }

    private void changeIndex(DfaNode node,int index) {
        int index0 = node.getIndex();
        Map<String,Integer> trans;
        for (Dfa.DfaNode n : nodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if(trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge,index);
                }
            }
        }
        for (Dfa.DfaNode n : tempNodes) {
            trans = n.getAllTransitions();
            for (String edge : trans.keySet()) {
                if(trans.get(edge) == index0) {
                    n.getAllTransitions().replace(edge,index);
                }
            }
        }
    }

    private NodeSet epsilonClosure(NodeSet set) {
        Deque<FaNode<Set<Integer>>> stack = new LinkedList<>();
        stack.addAll(set.nodes);
        List<FaNode<Set<Integer>>> result = set.nodes;
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
        return new NodeSet(result);
    }

    private NodeSet move(String edge, NodeSet set) {
        Deque<FaNode<Set<Integer>>> stack = new LinkedList<>();
        stack.addAll(set.nodes);
        List<FaNode<Set<Integer>>> result = new LinkedList<>();
        FaNode<Set<Integer>> t;
        Set<Integer> targets;
        while(!stack.isEmpty()) {
            t = stack.pop();
            targets = t.getTransition(edge);
            if(targets != null) {
                for (Integer i : targets) {
                    t = nfa.get(i);
                    if(!result.contains(t))
                        result.add(t);
                }
            }
        }
        return epsilonClosure(new NodeSet(result));
    }

    ArrayList<FaNode<Integer>> nfaToDfa() {
        List<NodeSet> nodeSets = new LinkedList<>();
        HashSet<String> edges = new HashSet<String>();
        Map<String, Set<Integer>> trans;
        int action;

        LinkedList<FaNode<Set<Integer>>> temp = new LinkedList<>();
        temp.add(nfa.get(0));
        NodeSet set = new NodeSet(temp);
        nodeSets.add(epsilonClosure(set));

        for(int i=0;i < nodeSets.size();i++) {
            nodes.add(new DfaNode(i));
            edges.clear();
            action = 9999;

            for (FaNode<Set<Integer>> node : nodeSets.get(i).nodes) {
                trans = node.getAllTransitions();
                for (String edge : trans.keySet()) {
                    edges.add(edge);
                }
                if (node.isAccepting()) {
                    if(node.getAction() < action) {
                        action = node.getAction();
                        nodes.get(i).setAccepting(action);
                    }
                }
            }

            for (String edge : edges) {
                set = move(edge, nodeSets.get(i));
                if (set.nodes.isEmpty())
                    break;
                if (!nodeSets.contains(set)) {
                    nodeSets.add(set);
                    nodes.get(i).addTransition(edge, (nodeSets.size()-1));
                } else {
                    nodes.get(i).addTransition(edge, nodeSets.indexOf(set));
                }
            }
        }

        //adjust sequence
        int i = -1;
        for (Dfa.DfaNode node : nodes) {
            if(node.isAccepting()) {
                nodes.remove(node);
                changeIndex(node,i);
                i--;
                tempNodes.add(node);
            }
        }
        accStart = nodes.size();
        nodes.addAll(tempNodes);
        accNum = nodes.size() - accStart;
        for (int j = 0;j < nodes.size();j++) {
            if(nodes.get(j).getIndex() != j) {
                changeIndex(nodes.get(j),j);
            }
        }


        ArrayList<FaNode<Integer>> dfa = new ArrayList<>(nodes.size());
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

}