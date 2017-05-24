package yacc.entity;

import java.util.ArrayList;
import java.util.List;

public class Productions {
    private List<Production> productions;
    private List<String> actions;
    private Integer start = null;

    public Productions() {
        this.productions = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    public void addProductionAndSetIndex(Production production, String action) {
        production.setIndex(productions.size());
        productions.add(production);
        actions.add(action);
    }

    public void addAugmentedStartAndSetIndex(Production production, String action) {
        start = productions.size();
        production.setIndex(productions.size());
        productions.add(production);
        actions.add(action);
    }

    public Production getStart() {
        return start == null ? null : productions.get(start);
    }

    public List<Production> getProductions() {
        return productions;
    }

    List<String> getActions() {
        return actions;
    }

    public Production getProduction(int index) {
        return productions.get(index);
    }
}
