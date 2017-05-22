package yacc;

import javafx.util.Pair;
import yacc.util.YaccStringBuilder;

import java.util.ArrayList;
import java.util.Map;

class ParserSourceGenerator {
    private static final String HEADER_NAME = "yacc.tab.h", SOURCE_NAME = "yacc.tab.cpp";
    ParserSourceGenerator() {
    }

    void generate(Map<Pair<Integer, Integer>, Integer> tableGoto,
                  Map<Pair<Integer, Integer>, Action> tableAction,
                  Productions productions, Symbols symbols, String programs) {
        generateHeader(symbols);
        generateSource(tableGoto, tableAction, productions, programs);
    }

    private void generateSource(Map<Pair<Integer, Integer>, Integer> tableGoto,
                                Map<Pair<Integer, Integer>, Action> tableAction,
                                Productions productions, String programs) {
        YaccStringBuilder source = new YaccStringBuilder();
        source.appendLine(programs);
        source.appendLine("void yyparse(void);");
        source.appendLine("int main() {")
                .appendLineWithTabs("yyparse();", 1)
                .appendLineWithTabs("return 0;", 1)
                .appendLine("}");
        YaccStringBuilder front = new YaccStringBuilder(), back = new YaccStringBuilder();
        front.appendLine("void yyparse(void) {");
        back.insertFrontLine("}");

        int numTabs = 1;


        System.out.println(source.toString());
        System.out.println(front.toString());
        System.out.println(back.toString());
    }

    private void generateHeader(Symbols symbols) {
        YaccStringBuilder header = new YaccStringBuilder();
//        header.appendLine("enum yytokentype {");
        for (Map.Entry<String, Integer> sym : symbols.getSymbols().entrySet()) {
            if (symbols.isTerminal(sym.getValue())
                    && (sym.getKey().length() != 1 || Character.isLetter(sym.getKey().charAt(0)))) {
                header.append("#define ").append(sym.getKey()).append(" ").appendLine(sym.getValue().toString());
//                header.append("\t").append(sym.getKey()).append(" = ").append(sym.getValue().toString())
//                        .appendLine(",");
            }
        }
    }
}
