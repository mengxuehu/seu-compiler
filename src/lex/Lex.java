package lex;

import java.nio.file.Paths;
import java.util.*;

public class Lex {
    Lex(String source) {
        LexSourceParser lexSourceParser = new LexSourceParser();
        ReParser reParser = new ReParser();

        lexSourceParser.parse(source);
        Map<String, String> rules = lexSourceParser.getRules();

        ArrayList<String> ruleAction = new ArrayList<>(rules.size());
        Map<Integer, String[]> postfixRes = new LinkedHashMap<>(rules.size());

        int i = 0;
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            ruleAction.set(i, entry.getValue());
            postfixRes.put(i++, reParser.parse(entry.getKey()));
        }

        new LexerSourceGenerator().generate(
                new Dfa(new Nfa().construct(postfixRes)).nfaToDfa(), ruleAction, lexSourceParser.getUserRoutines());
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.l").toString();
        new Lex(sourcePath);
    }
}
