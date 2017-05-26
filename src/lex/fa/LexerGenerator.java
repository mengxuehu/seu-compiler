package lex.fa;


import lex.fa.node.DfaNode;
import lex.fa.node.FaNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LexerGenerator {
    private static final String FILENAME = "seulex.cpp";

    public void generate(Map<Integer, String[]> postfixRes, String[] ruleAction, String userRoutines) {
        Dfa dfa = new Dfa(new Nfa().construct(postfixRes));
        ArrayList<DfaNode> dfaNodes = dfa.nfaToDfa();

        MiniDfa miniDfa = new MiniDfa();
        miniDfa.miniDfa(dfaNodes, dfa.getAccStart(), dfa.getAccNum());

        doGenerate(miniDfa.getDfa(), ruleAction, userRoutines);
    }

    private void doGenerate(ArrayList<DfaNode> dfa, String[] ruleAction, String userRoutines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {
            bw.write(userRoutines + "\n");
            bw.write("#include <sstream>\n");
            bw.write("#include <ifstream>\n");
            bw.write("#include <iostream>");
            bw.write("extern int yylex() {\n");
            bw.write("\tint state = " + dfa.get(0).getIndex() + ";\n");
            bw.write("\tint i = 0;\n");
            bw.write("\tstatic std::ifstream in(\"main.cpp\")\n");
            //start
            bw.write("\twhile(true) {\n");
            bw.write("\t\tchar next = in.get();\n");
            bw.write("\t\tstd::stringstream str;\n");
            bw.write("\t\tstr << next;\n");
            bw.write("\t\tstring str = stream.str()\n");
            bw.write("\t\tswitch(state) {\n");
            for (FaNode<Integer> dfaNode : dfa) {
                bw.write("\t\t\tcase " + dfaNode.getIndex() + ":\n");
                Map<String, Integer> trans = dfaNode.getAllTransitions();

                for (String edge : trans.keySet()) {
                    if (edge.length() > 1 && edge.charAt(0) == '!') {
                        bw.write("\t\t\t\tif(");
                        for (int i = 1; i < edge.length(); i++) {
                            String sub = edge.substring(i, i + 1);
                            bw.write("\t\t\t\tstr != \"" + sub + "\"");
                            if (i != edge.length() - 1)
                                bw.write("&&");
                        }
                        bw.write(") {\n");
                    } else {
                        bw.write("\t\t\t\tif(str == \"" + edge + "\") {\n");
                    }
                    bw.write("\t\t\t\t\tstate = " + trans.get(edge) + ";\n");
                    bw.write("\t\t\t\t\tbreak;\n");
                    bw.write("\t\t\t\t}\n");
                }
                bw.write("in.seekg(-1, ios::cur)");
                if (dfaNode.isAccepting()) {
                    bw.write("\t\t\t\treturn " + ruleAction[dfaNode.getAction()] + ";\n");
                } else {
                    bw.write("\t\t\t\treturn -1;\n");
                }
            }
            bw.write("\t\t}\n");

            //end
            bw.write("\t}\n");
            bw.write("}\n");
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}