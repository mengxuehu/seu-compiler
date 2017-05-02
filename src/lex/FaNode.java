package lex;

import java.util.HashMap;
import java.util.Map;

abstract class FaNode<V> {
    private int index;
    private boolean terminal = false;
    private Integer action;

    Map<String, V> transitions;

    FaNode(int index) {
        this.index = index;
        transitions = new HashMap<>();
    }

    void setIndex(int index) {
        this.index = index;
    }

    int getIndex() {
        return index;
    }

    boolean isTerminal() {
        return terminal;
    }

    void setTerminal(int action) {
        this.terminal = true;
        this.action = action;
    }

    void clearTerminal() {
        this.terminal = false;
        this.action = null;
    }

    public Integer getAction() {
        return action;
    }

    abstract void addTransition(String edge, int target);

    V getTransition(String edge) {
        return transitions.get(edge);
    }

    abstract void addAllTransitions(Map<String, V> transitions);

    Map<String, V> getAllTransitions() {
        return transitions;
    }

}