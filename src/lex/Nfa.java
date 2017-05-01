package lex;

import javafx.util.Pair;

import java.util.*;


class NfaNode {
    private int index;
    private boolean terminal = false;
    private HashSet<Pair<String, Integer>> transitions = new HashSet<>();

    NfaNode(int index) {
        this.index = index;
    }

    NfaNode(int index, String edge, int target) {
        this.index = index;
        this.addTransition(edge, target);
    }

    int getIndex() {
        return index;
    }

    boolean isTerminal() {
        return terminal;
    }

    void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    void addTransition(String edge, int target) {
        transitions.add(new Pair<>(edge, target));
    }

    void addAllTransitions(NfaNode node) {
        this.transitions.addAll(node.transitions);
    }
}


class Nfa {
    private LinkedList<NfaNode> nodes = new LinkedList<>();
    private static int indexes = 0;

    Nfa() {

    }

    Nfa reToNfa(String[] postfixRe) {
        Deque<Nfa> stack = new LinkedList<>();
        int i = 0;
        while (i != postfixRe.length) {
            switch (postfixRe[i].charAt(0)) {
                case '*':
                    Nfa nfa = stack.pop();
                    nfa.closure();
                    stack.push(nfa);
                    break;
                case '|':
                    nfa = stack.pop();
                    nfa.union(stack.pop());
                    stack.push(nfa);
                    break;
                case '@':
                    nfa = stack.pop();
                    nfa.concatenation(stack.pop());
                    stack.push(nfa);
                    break;
                default:
                    stack.push(new Nfa(postfixRe[i]));
                    break;
            }
            ++i;
        }
        Nfa nfa = stack.pop();
        assert stack.isEmpty();
        nfa.nodes.getLast().setTerminal(true);
        return nfa;
    }

    private Nfa(String operand) {
        int begin = getNextIndex(), end = getNextIndex();
        nodes.add(new NfaNode(begin, operand, end));
        nodes.add(new NfaNode(end));
    }

    List<NfaNode> merge(Collection<Nfa> nfas) {
        NfaNode start = new NfaNode(getNextIndex());
        LinkedList<NfaNode> allNodes = new LinkedList<>();
        allNodes.add(start);
        for (Nfa nfa : nfas) {
            start.addTransition("", nfa.nodes.getFirst().getIndex());
            allNodes.addAll(nfa.nodes);
        }
        ArrayList<NfaNode> nfa = new ArrayList<>(allNodes.size());
        for (NfaNode node : allNodes) {
            nfa.set(node.getIndex(), node);
        }
        return nfa;
    }

    private int getNextIndex() {
        return indexes++;
    }

    private void closure() {
        int oldBegin = nodes.getFirst().getIndex();

        nodes.addFirst(new NfaNode(getNextIndex()));
        nodes.addLast(new NfaNode(getNextIndex()));

        nodes.getLast().addTransition("", oldBegin);
        nodes.getLast().addTransition("", nodes.getLast().getIndex());

        nodes.getFirst().addTransition("", oldBegin);
        nodes.getFirst().addTransition("", nodes.getLast().getIndex());
    }

    private void union(Nfa nfa) {
        NfaNode newFirst = new NfaNode(getNextIndex(), "", this.nodes.getFirst().getIndex());
        newFirst.addTransition("", nfa.nodes.getFirst().getIndex());

        NfaNode newLast = new NfaNode(getNextIndex());
        this.nodes.getLast().addTransition("", newLast.getIndex());
        nfa.nodes.getLast().addTransition("", newLast.getIndex());

        this.nodes.addAll(nfa.nodes);
        this.nodes.addFirst(newFirst);
        this.nodes.addLast(newLast);
    }

    private void concatenation(Nfa nfa) {
        NfaNode node = nfa.nodes.removeFirst();
        this.nodes.getLast().addAllTransitions(node);
        this.nodes.addAll(nfa.nodes);
    }

}
