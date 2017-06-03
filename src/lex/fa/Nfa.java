package lex.fa;

import javafx.util.Pair;
import lex.ReParser;
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
            String[] source = {"\\(", "\\)", "\\[", "\\]", "\\*", "\\+", "\\.", "\\-", "\\|", "\\@", "\\?", "\\^", "\\!"};
            int[] target = {'(', ')', '[', ']', '*', '+', '.', '-', '|', '@', '?', '^', '!'};
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

    private void checkSpace(Nfa nfa, int index) {
        for (NfaNode nfaNode : nfa.nodes) {
            if (nfaNode.getIndex() == index) {
                for (Map.Entry<String, Set<Integer>> entry : nfaNode.getAllTransitions().entrySet()) {
                    if (entry.getKey().equals(" ")) {
                        System.out.println("haha");
                    } else if (entry.getKey().equals("")){
                        for (Integer next : entry.getValue()) {
                            checkSpace(nfa, next);
                        }
                    }
                }
                break;
            }
        }
    }

    ArrayList<NfaNode> construct(Map<Integer, String[]> postfixRes) {
        nodes = new LinkedList<>();
        indexes = 0;
        nodes.add(new NfaNode(getNextIndex()));

        for (Map.Entry<Integer, String[]> entry : postfixRes.entrySet()) {
            Nfa nfa = reToNfa(entry.getValue(), entry.getKey());
            checkSpace(nfa, nfa.nodes.getFirst().getIndex());
            nodes.getFirst().addTransition("", nfa.nodes.getFirst().getIndex());
            nodes.addAll(nfa.nodes);
        }

        ArrayList<NfaNode> nfa = new ArrayList<>(getIndexUpperBound());
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
        for (NfaNode nfaNode : nfa.nodes) {
            for (Set<Integer> next : nfaNode.getAllTransitions().values()) {
                if (next.remove(nfa.nodes.getFirst().getIndex())) {
                    next.add(this.nodes.getFirst().getIndex());
                }
                if (next.remove(nfa.nodes.getLast().getIndex())) {
                    next.add(this.nodes.getLast().getIndex());
                }
            }
        }
        this.nodes.getFirst().addAllTransitions(nfa.nodes.removeFirst().getAllTransitions());
        this.nodes.getLast().addAllTransitions(nfa.nodes.removeLast().getAllTransitions());
        NfaNode tmpLast = this.nodes.removeLast();
        this.nodes.addAll(nfa.nodes);
        this.nodes.add(tmpLast);
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
