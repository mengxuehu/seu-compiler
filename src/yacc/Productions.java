package yacc;

import java.util.ArrayList;
import java.util.List;

class Productions {
    private List<Production> productions;
    private List<String> actions;
    private Integer start = null;

    Productions() {
        this.productions = new ArrayList<>();
        this.actions = new ArrayList<>();
    }

    void addProductionAndSetIndex(Production production, String action) {
        productions.add(production);
        actions.add(action);
    }

    void addAugmentedStartAndSetIndex(Production production, String action) {
        start = productions.size();
        production.setIndex(productions.size());
        productions.add(production);
        actions.add(action);
    }

    Production getStart() {
        return start == null ? null : productions.get(start);
    }

    List<Production> getProductions() {
        return productions;
    }

    List<String> getActions() {
        return actions;
    }

    Production getProduction(int index) {
        return productions.get(index);
    }
}
