package lex;

import lex.fa.LexerGenerator;

import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class Lex {
    Lex(String source) {
        LexSourceParser lexSourceParser = new LexSourceParser();
        ReParser reParser = new ReParser();
        lexSourceParser.parse(source);
        Map<String, String> rules = lexSourceParser.getRules();

        String[] ruleAction = new String[rules.size()];
        Map<Integer, String[]> postfixRes = new LinkedHashMap<>(rules.size());
        int i = 0;
        for (Map.Entry<String, String> entry : rules.entrySet()) {
            ruleAction[i] = entry.getValue();
            postfixRes.put(i++, reParser.parse(entry.getKey()));
        }

        new LexerGenerator().generate(postfixRes, ruleAction, lexSourceParser.getUserRoutines());
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.l").toString();
        new Lex(sourcePath);
    }
}
