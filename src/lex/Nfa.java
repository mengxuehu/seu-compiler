package lex;

import java.util.*;


class Nfa {

    private class NfaNode extends FaNode<Set<Integer>> {
        NfaNode(int index) {
            super(index);
        }

        NfaNode(int index, String edge, int target) {
            super(index);
            this.addTransition(edge, target);
        }

        @Override
        void addTransition(String edge, int target) {
            if (this.transitions.containsKey(edge)) {
                this.transitions.get(edge).add(target);
            } else {
                Set<Integer> tmp = new HashSet<>();
                tmp.add(target);
                this.transitions.put(edge, tmp);
            }
        }

        @Override
        void addAllTransitions(Map<String, Set<Integer>> transitions) {
            for (Map.Entry<String, Set<Integer>> entry : transitions.entrySet()) {
                if (this.transitions.containsKey(entry.getKey())) {
                    this.transitions.get(entry.getKey()).addAll(entry.getValue());
                } else {
                    this.transitions.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private LinkedList<NfaNode> nodes;
    private static int indexes = 0;
    private static Map<String, String> escapeChars = null;

    Nfa() {
        initializeEscapeChars();
    }

    ArrayList<FaNode<Set<Integer>>> construct(Map<Integer, String[]> postfixRes) {
        nodes = new LinkedList<>();
        indexes = 0;
        nodes.add(new NfaNode(getNextIndex()));

        for (Map.Entry<Integer, String[]> entry : postfixRes.entrySet()) {
            Nfa nfa = reToNfa(entry.getValue(), entry.getKey());
            nodes.getFirst().addTransition("", nfa.nodes.getFirst().getIndex());
            nodes.addAll(nfa.nodes);
        }

        ArrayList<FaNode<Set<Integer>>> nfa = new ArrayList<>(nodes.size());
        for (NfaNode node : nodes) {
            nfa.set(node.getIndex(), node);
        }
        return nfa;
    }

    private Nfa(String operand) {
        nodes = new LinkedList<>();
        int begin = getNextIndex(), end = getNextIndex();
        nodes.add(new NfaNode(begin, operand, end));
        nodes.add(new NfaNode(end));
    }

    private Nfa reToNfa(String[] postfixRe, int action) {
        Deque<Nfa> stack = new LinkedList<>();
        for (String s : postfixRe) {
            switch (s.charAt(0)) {
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
                    stack.push(new Nfa(processOperand(s)));
                    break;
            }
        }

        Nfa nfa = stack.pop();
        assert stack.isEmpty();
        nfa.nodes.getLast().setAccepting(action);
        return nfa;
    }

    private int getNextIndex() {
        return indexes++;
    }

    private void closure() {
        int oldBegin = nodes.getFirst().getIndex();
        int newEnd = getNextIndex();

        nodes.getLast().addTransition("", oldBegin);
        nodes.getLast().addTransition("", newEnd);

        nodes.addFirst(new NfaNode(getNextIndex()));
        nodes.addLast(new NfaNode(newEnd));

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
        this.nodes.getLast().addAllTransitions(node.transitions);
        this.nodes.addAll(nfa.nodes);
    }

    private void initializeEscapeChars() {
        if (escapeChars == null) {
            escapeChars = new HashMap<>();
            String[] source = {"\\a", "\\b", "\\f", "\\n", "\\r", "\\t", "\\v", "\\\\", "\\'", "\\\"", "\\?"};
            int[] target = {0x07, 0x08, 0x0C, 0x0A, 0x0D, 0x09, 0x0B, 0x5C, 0x27, 0x22, 0x3F};
            assert source.length == target.length;
            for (int i = 0; i < source.length; i++) {
                escapeChars.put(source[i], String.valueOf((char) target[i]));
            }
            escapeChars.put(ReParser.epsilon, "");
        }
    }

    private String processOperand(String operand) {
        if (escapeChars.containsKey(operand)) {
            operand = escapeChars.get(operand);
        }
        return operand;
    }

    public static void main(String[] args) {
        Nfa nfa = new Nfa();
        nfa.initializeEscapeChars();
        for (String s : escapeChars.keySet()) {
            System.out.println(s + " " + escapeChars.get(s));
        }
    }

}
