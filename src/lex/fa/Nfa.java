package lex.fa;

import lex.ReParser;
import lex.fa.node.FaNode;
import lex.fa.node.NfaNode;

import java.util.*;


class Nfa {

    private static int indexes = 0;
    private static Map<String, String> escapeChars = null;
    private LinkedList<NfaNode> nodes;

    Nfa() {
        initializeEscapeChars();
    }

    private void initializeEscapeChars() {
        if (escapeChars == null) {
            escapeChars = new HashMap<>();
            //.-|@?!
            String[] source = {"\\a", "\\b", "\\f", "\\n", "\\r", "\\t", "\\v", "\\\\", "\\'", "\\\"", "\\?",
                    "\\(", "\\)", "\\[", "\\]", "\\*", "\\+", "\\.", "\\-", "\\|", "\\@", "\\?", "\\!"};
            int[] target = {0x07, 0x08, 0x0C, 0x0A, 0x0D, 0x09, 0x0B, 0x5C, 0x27, 0x22, 0x3F,
                    '(', ')', '[', ']', '*', '+', '.', '-', '|', '@', '?', '!'};
            for (int i = 0; i < source.length; i++) {
                escapeChars.put(source[i], String.valueOf((char) target[i]));
            }
            escapeChars.put(ReParser.epsilon, "");
        }
    }

    private Nfa(String operand) {
        nodes = new LinkedList<>();
        int begin = getNextIndex(), end = getNextIndex();
        nodes.add(new NfaNode(begin, operand, end));
        nodes.add(new NfaNode(end));
    }

    private int getNextIndex() {
        return indexes++;
    }

    public static void main(String[] args) {
        Nfa nfa = new Nfa();
        nfa.initializeEscapeChars();
        for (String s : escapeChars.keySet()) {
            System.out.println(s + " " + escapeChars.get(s));
        }
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

        ArrayList<FaNode<Set<Integer>>> nfa = new ArrayList<>(getIndexUpperBound());
        for (int i = 0; i < getIndexUpperBound(); i++) {
            nfa.add(null);
        }
        for (NfaNode node : nodes) {
            if (nfa.get(node.getIndex()) == null) {
                nfa.set(node.getIndex(), node);
            } else {
                System.err.println("error");
            }
        }
        return nfa;
    }

    private Nfa reToNfa(String[] postfixRe, int action) {
        Deque<Nfa> stack = new LinkedList<>();
        for (String s : postfixRe) {
            switch (s.charAt(0)) {
                case '*':
                    stack.push(stack.pop().closure());
                    break;
                case '|':
                    Nfa nfa = stack.pop();
                    stack.push(stack.pop().union(nfa));
                    break;
                case '@':
                    nfa = stack.pop();
                    stack.push(stack.pop().concatenation(nfa));
                    break;
                default:
                    stack.push(new Nfa(processOperand(s)));
                    break;
            }
        }

        Nfa nfa = stack.pop();
        nfa.nodes.getLast().setAccepting(action);
        return nfa;
    }

    private int getIndexUpperBound() {
        return indexes;
    }

    private Nfa closure() {
        int oldBegin = nodes.getFirst().getIndex();
        int newEnd = getNextIndex();

        nodes.getLast().addTransition("", oldBegin);
        nodes.getLast().addTransition("", newEnd);

        nodes.addFirst(new NfaNode(getNextIndex()));
        nodes.addLast(new NfaNode(newEnd));

        nodes.getFirst().addTransition("", oldBegin);
        nodes.getFirst().addTransition("", nodes.getLast().getIndex());
        return this;
    }

    private Nfa union(Nfa nfa) {
        NfaNode newFirst = new NfaNode(getNextIndex(), "", this.nodes.getFirst().getIndex());
        newFirst.addTransition("", nfa.nodes.getFirst().getIndex());

        NfaNode newLast = new NfaNode(getNextIndex());
        this.nodes.getLast().addTransition("", newLast.getIndex());
        nfa.nodes.getLast().addTransition("", newLast.getIndex());

        this.nodes.addAll(nfa.nodes);
        this.nodes.addFirst(newFirst);
        this.nodes.addLast(newLast);
        return this;
    }

    private Nfa concatenation(Nfa nfa) {
        NfaNode node = nfa.nodes.removeFirst();
        this.nodes.getLast().addAllTransitions(node.getAllTransitions());
        this.nodes.addAll(nfa.nodes);
        return this;
    }

    private String processOperand(String operand) {
        if (escapeChars.containsKey(operand)) {
            operand = escapeChars.get(operand);
        }
        return operand;
    }

}
