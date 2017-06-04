package lex.fa;


import lex.fa.node.DfaNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

class SourceGenerator {
    private static final String FILENAME = "lex.yy.cpp";

    SourceGenerator(ArrayList<DfaNode> dfa, String[] ruleAction, String userRoutines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILENAME))) {

            bw.write("#include <fstream>\n");
            bw.write("#include <iostream>\n");
            bw.write("std::string yytext;\n");
            bw.write("std::ifstream yyin;\n");
            bw.write(userRoutines + "\n");
            bw.write("extern int yylex() {\n");
            bw.write("\tint state = 0;\n");
            bw.write("\tint i = 0;\n");
            bw.write("\tyytext.erase();\n");
            bw.write("\tchar next;\n");
            //start
            bw.write("\twhile(true) {\n");
            bw.write("\t\tif ((next = yyin.get()) == EOF && yytext.empty()) {\n" +
                    "\t\t\treturn -2;\n" +
                    "\t\t}\n");
//            bw.write("\t\tstd::stringstream str;\n");
//            bw.write("\t\tstr << next;\n");
//            bw.write("\t\tstring str = stream.str()\n");
            bw.write("\t\tswitch(state) {\n");
            for (DfaNode dfaNode : dfa) {
                bw.write("\t\t\tcase " + dfaNode.getIndex() + ":\n");
                Map<String, Integer> trans = dfaNode.getAllTransitions();

                for (String edge : trans.keySet()) {
//                    if (edge.length() > 1 && edge.charAt(0) == '!') {
//                        bw.write("\t\t\t\tif(");
//                        for (int i = 1; i < edge.length(); i++) {
//                            String sub = edge.substring(i, i + 1);
//                            bw.write("\t\t\t\tstr != \"" + sub + "\"");
//                            if (i != edge.length() - 1)
//                                bw.write("&&");
//                        }
//                        bw.write(") {\n");
//                    } else {
//                    bw.write("\t\t\t\tif(str == \'" + edge + "\') {\n");

                    if (edge.equals("'")) {
                        bw.write("\t\t\t\tif(next == '\\'') {\n");
                    } else {
                        bw.write("\t\t\t\tif(next == '");
                        bw.write(edge + "') {\n");
                    }

//                    }
                    bw.write("\t\t\t\t\tstate = " + trans.get(edge) + ";\n");
                    bw.write("\t\t\t\t\tyytext += next;\n");
                    bw.write("\t\t\t\t\tbreak;\n");
                    bw.write("\t\t\t\t}\n");
                }
                bw.write("\t\t\t\tyyin.seekg(-1, std::ios::cur);\n");
                if (dfaNode.isAccepting()) {
                    bw.write("\t\t\t\t" + ruleAction[dfaNode.getAction()] + ";\n");
                    bw.write("\t\t\t\tstate = 0;\n");
                    bw.write("\t\t\t\tyytext.erase();\n");
                    bw.write("\t\t\t\tbreak;\n");
//                    bw.write("\t\t\t\treturn " + ruleAction[dfaNode.getAction()] + ";\n");
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
