package yacc;


import javafx.util.Pair;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class Yacc {
    Yacc(String source) {
        YaccSourceParser yaccSourceParser = new YaccSourceParser();
        yaccSourceParser.parse(source);
        String programs = yaccSourceParser.getPrograms();
        Productions productions = yaccSourceParser.getProductions();
        Symbols symbols = yaccSourceParser.getSymbols();

        LR1 lr1 = new LR1();
        lr1.parse(productions, symbols);
        Map<Pair<Integer, Integer>, Integer> tableGoto = lr1.getTableGoto();
        Map<Pair<Integer, Integer>, Action> tableAction = lr1.getTableAction();
        Set<ItemSet> collection = lr1.getCollection();
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.y").toString();
        new Yacc(sourcePath);
    }
}
