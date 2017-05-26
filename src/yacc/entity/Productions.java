package yacc.entity;

import java.util.ArrayList;
import java.util.List;

public class Productions {
    private List<Production> productions;
    private Integer start = null;

    public Productions() {
        this.productions = new ArrayList<>();
    }

    public void addProductionAndSetIndex(Production production) {
        production.setIndex(productions.size());
        productions.add(production);
    }

    public void addAugmentedStartAndSetIndex(Production production) {
        start = productions.size();
        production.setIndex(productions.size());
        productions.add(production);
    }

    public Production getStart() {
        return start == null ? null : productions.get(start);
    }

    public List<Production> getProductions() {
        return productions;
    }

    public Production getProduction(int index) {
        return productions.get(index);
    }
}
