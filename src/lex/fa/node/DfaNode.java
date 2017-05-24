package lex.fa.node;

import java.util.Map;


public class DfaNode extends FaNode<Integer> {
    public DfaNode(int index) {
        super(index);
    }

    public DfaNode(int index, String edge, int target) {
        super(index);
        this.addTransition(edge, target);
    }

    @Override
    public void addTransition(String edge, int target) {
        this.getAllTransitions().put(edge, target);
    }

    @Override
    public void addAllTransitions(Map<String, Integer> transitions) {
    }
}
