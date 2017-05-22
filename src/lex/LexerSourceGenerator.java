package lex;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class LexerSourceGenerator {
    private static final String FILENAME = "seulex.cpp";
    LexerSourceGenerator() {
    }

    void generate(ArrayList<FaNode<Integer>> dfa, ArrayList<String> ruleAction, String userRoutines) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME));
        bw.write("#include <sstream>\n");
        bw.write("#include <ifstream>\n");
        bw.write("#include <iostream>");
        bw.write("using namespace std;\n");
        bw.write("int yylex() {\n");
        bw.write("\tint state = " + dfa.get(0).getIndex() + ";\n");
        bw.write("\tint i = 0;\n");
        bw.write("\tstatic ifstream in(\"main.cpp\")\n");
        //start
        bw.write("\twhile(true) {\n");
        bw.write("\t\tchar next = in.get();\n");
        bw.write("\t\tstringstream str;\n");
        bw.write("\t\tstr << next;\n");
        bw.write("\t\tstring str = stream.str()\n");
        bw.write("\t\tswitch(state) {\n");
        for (FaNode<Integer> dfaNode : dfa) {
            bw.write("\t\t\tcase " + dfaNode.getIndex() + ":\n");
            Map<String, Integer> trans = dfaNode.getAllTransitions();

            for (String edge : trans.keySet()) {
                if (edge.length() > 1 && edge.charAt(0) == '!'){
                    bw.write("\t\t\t\tif(str != \"" + edge + "\") {\n");
                }else {
                    bw.write("\t\t\t\tif(str == \"" + edge + "\") {\n");
                }
                bw.write("\t\t\t\t\tstate = " + trans.get(edge) + ";\n");
                bw.write("\t\t\t\t\tbreak;\n");
                bw.write("\t\t\t\t}\n");
            }
            bw.write("in.seekg(-1, ios::cur)");
            if (dfaNode.isAccepting()){
                bw.write("\t\t\t\treturn " + ruleAction.get(dfaNode.getAction()) + ";\n");
            } else {
                bw.write("\t\t\t\treturn -1;\n");
            }
        }
        bw.write("\t\t}\n");

        //end
        bw.write("\t}\n");
        bw.write("}\n");
        bw.flush();
        return;
    }

}
