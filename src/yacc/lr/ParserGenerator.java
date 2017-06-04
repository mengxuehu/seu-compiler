package yacc.lr;

import javafx.util.Pair;
import yacc.entity.Associativity;
import yacc.entity.Precedence;
import yacc.entity.Productions;
import yacc.entity.Symbols;

import java.util.Map;
import java.util.Set;

public class ParserGenerator {

    public void generate(Productions productions, Symbols symbols, String programs,
                         Map<Integer, Precedence> precedence, Map<Integer, Associativity> associativity) {

        long t1 = System.currentTimeMillis();
        LR1 lr1 = new LR1();
        lr1.parse(productions, symbols, precedence, associativity);
        Map<Pair<Integer, Integer>, Integer> tableGoto = lr1.getTableGoto();
        Map<Pair<Integer, Integer>, Action> tableAction = lr1.getTableAction();
        Set<ItemSet> collection = lr1.getCollection();

        long t2 = System.currentTimeMillis();
        System.out.println("LR1: " + (t2 - t1));

        LALR1 lalr1 = new LALR1();

        if (lalr1.generateLALR1(collection, tableGoto, tableAction)) {
            tableGoto = lalr1.getTableGoto();
            tableAction = lalr1.getTableAction();
        }

        long t3 = System.currentTimeMillis();
        System.out.println("LALR1: " + (t3 - t2));

        new SourceGenerator(productions, symbols, programs, tableGoto, tableAction);

        System.out.println("Generating Code: " + (System.currentTimeMillis() - t3));
    }
}
