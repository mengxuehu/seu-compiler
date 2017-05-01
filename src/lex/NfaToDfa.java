package lex;

import javafx.util.Pair;

import java.util.*;

public class NfaToDfa {
    private List<NfaNode> nfa;
    private List<NfaNode> dfa;
    String[] edges;//all symbols

    NfaToDfa(List<NfaNode> nfa) {
        this.nfa = nfa;
        this.dfa = new LinkedList<>();
    }

    private NodeSet epsilonClosure(NodeSet set) {
        Deque<NfaNode> stack = new LinkedList<>();
        stack.addAll(set.nodes);
        List<NfaNode> result = set.nodes;
        NfaNode t;
        Set<Pair<String, Integer>> trans;
        while(!stack.isEmpty()) {
            t = stack.pop();
            trans = t.getTransitions();
            for(int i=0;i < trans.size();i++) {
                if(trans.contains(new Pair("",i))) {
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
        Deque<NfaNode> stack = new LinkedList<>();
        stack.addAll(set.nodes);
        List<NfaNode> result = new LinkedList<>();
        NfaNode t;
        Set<Pair<String, Integer>> trans;
        while(!stack.isEmpty()) {
            t = stack.pop();
            trans = t.getTransitions();
            for(int i=0;i < trans.size();i++) {
                if(trans.contains(new Pair(edge,i))) {
                    t = nfa.get(i);
                    if(!result.contains(t))
                        result.add(t);
                }
            }
        }
        return epsilonClosure(new NodeSet(result));
    }

    List<NfaNode> transform() {
        List<NodeSet> nodeSets = new LinkedList<>();
        LinkedList<NfaNode> temp = new LinkedList<>();
        temp.add(nfa.get(0));
        NodeSet set = new NodeSet(temp);
        nodeSets.add(epsilonClosure(set));
        for(int i=0;i < nodeSets.size();i++) {
            dfa.add(new NfaNode(i));
            for(int j=0;j < nodeSets.get(j).nodes.size();j++) {
                if(nodeSets.get(i).nodes.get(j).isTerminal())
                {dfa.get(i).setTerminal(true);break;}
            }

            for(int k=0;k < edges.length;k++) {
                set = move(edges[k],nodeSets.get(i));
                if(set.nodes.isEmpty())
                    break;
                if(!nodeSets.contains(set)) {
                    nodeSets.add(set);
                    dfa.get(i).addTransition(edges[k],nodeSets.size()-1);
                } else {
                    dfa.get(i).addTransition(edges[k],nodeSets.indexOf(set));
                }
            }
        }

        //min

        return dfa;
    }

    class NodeSet {
        List<NfaNode> nodes;

        NodeSet(List<NfaNode> nodes) {
            this.nodes = nodes;
        }
    }
}