package lex.fa.node;

import java.util.HashMap;
import java.util.Map;

public abstract class FaNode<V> {
    private Map<String, V> transitions;
    private int index;
    private boolean accepting = false;
    private Integer action;

    public FaNode(int index) {
        this.index = index;
        transitions = new HashMap<>();
    }

    public int getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    public boolean isAccepting() {
        return accepting;
    }

    public void setAccepting(int action) {
        this.accepting = true;
        this.action = action;
    }

    void clearAccepting() {
        this.accepting = false;
        this.action = null;
    }

    public Integer getAction() {
        return action;
    }

    public abstract void addTransition(String edge, int target);

    public V getTransition(String edge) {
        return transitions.get(edge);
    }

    public abstract void addAllTransitions(Map<String, V> transitions);

    public Map<String, V> getAllTransitions() {
        return transitions;
    }

}