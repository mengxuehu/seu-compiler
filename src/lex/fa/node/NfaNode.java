package lex.fa.node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class NfaNode extends FaNode<Set<Integer>> {
    public NfaNode(int index) {
        super(index);
    }

    public NfaNode(int index, String edge, int target) {
        super(index);
        this.addTransition(edge, target);
    }

    @Override
    public void addTransition(String edge, int target) {
        if (this.getAllTransitions().containsKey(edge)) {
            this.getAllTransitions().get(edge).add(target);
        } else {
            Set<Integer> tmp = new HashSet<>();
            tmp.add(target);
            this.getAllTransitions().put(edge, tmp);
        }
    }

    @Override
    public void addAllTransitions(Map<String, Set<Integer>> transitions) {
        for (Map.Entry<String, Set<Integer>> entry : transitions.entrySet()) {
            if (this.getAllTransitions().containsKey(entry.getKey())) {
                this.getAllTransitions().get(entry.getKey()).addAll(entry.getValue());
            } else {
                this.getAllTransitions().put(entry.getKey(), entry.getValue());
            }
        }
    }
}
