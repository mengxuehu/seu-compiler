package yacc;


import yacc.entity.Associativity;
import yacc.entity.Precedence;
import yacc.entity.Productions;
import yacc.entity.Symbols;
import yacc.lr.ParserGenerator;

import java.nio.file.Paths;
import java.util.Map;

public class Yacc {
    public Yacc(String source) {
        YaccSourceParser yaccSourceParser = new YaccSourceParser();
        yaccSourceParser.parse(source);
        String programs = yaccSourceParser.getPrograms();
        Productions productions = yaccSourceParser.getProductions();
        Symbols symbols = yaccSourceParser.getSymbols();
        Map<Integer, Precedence> precedence = yaccSourceParser.getPrecedence();
        Map<Integer, Associativity> associativity = yaccSourceParser.getAssociativity();

        new ParserGenerator().generate(productions, symbols, programs, precedence, associativity);
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.y").toString();
        new Yacc(sourcePath);
    }
}
