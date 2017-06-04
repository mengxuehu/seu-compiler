package lex.fa;


import lex.fa.node.DfaNode;
import lex.fa.node.NfaNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LexerGenerator {

    public void generate(Map<Integer, String[]> postfixRes, String[] ruleAction, String userRoutines) {
        long t1 = System.currentTimeMillis();

        ArrayList<NfaNode> nfa = new Nfa().construct(postfixRes);

        long t2 = System.currentTimeMillis();
        System.out.println("NFA: " + (t2 - t1));

        Dfa dfa = new Dfa(nfa);
        ArrayList<DfaNode> dfaNodes = dfa.nfaToDfa();

        long t3 = System.currentTimeMillis();
        System.out.println("DFA: " + (t3 - t2));

        MiniDfa miniDfa = new MiniDfa();
        miniDfa.miniDfa(dfaNodes, dfa.getAccStart(), dfa.getAccNum());

        long t4 = System.currentTimeMillis();
        System.out.println("Min DFA: " + (t4 - t3));

        new SourceGenerator(miniDfa.getDfa(), ruleAction, userRoutines);

        long t5 = System.currentTimeMillis();
        System.out.println("Generating Code: " + (t5 - t4));
    }
}
