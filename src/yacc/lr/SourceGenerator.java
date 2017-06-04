package yacc.lr;


import javafx.util.Pair;
import yacc.entity.Production;
import yacc.entity.Productions;
import yacc.entity.Symbols;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

class SourceGenerator {
    private static final String HEADER_NAME = "y.tab.h", SOURCE_NAME = "y.tab.cpp";
    private Productions productions;
    private Symbols symbols;
    private String programs;
    private Map<Pair<Integer, Integer>, Integer> tableGoto;
    private Map<Pair<Integer, Integer>, Action> tableAction;


    SourceGenerator(Productions productions, Symbols symbols, String programs,
                           Map<Pair<Integer, Integer>, Integer> tableGoto,
                           Map<Pair<Integer, Integer>, Action> tableAction) {
        this.productions = productions;
        this.symbols = symbols;
        this.programs = programs;
        this.tableGoto = tableGoto;
        this.tableAction = tableAction;
        generate();
    }

    private void generate() {
        generateHeader();
        generateSource();
    }

    private void generateHeader() {
        StringBuilder header = new StringBuilder();
        header.append("#include <string>\n\n");

        for (Map.Entry<String, Integer> sym : symbols.getSymbols().entrySet()) {
            if (symbols.isTerminal(sym.getValue())
                    && (sym.getKey().length() != 1 || Character.isLetter(sym.getKey().charAt(0)))) {
                header.append("#define ").append(sym.getKey()).append(" ")
                        .append(sym.getValue().toString()).append('\n');
            }
        }

//        System.out.println(header.toString());
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(HEADER_NAME))) {
            bw.write(header.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateSource() {
        StringBuilder source = new StringBuilder();
        source.append(programs).append("\n\n");

        source.append("#include <map>\n")
                .append("#include <stack>\n")
                .append("#include <string>\n")
                .append("#include <utility>\n")
                .append("#include <iostream>\n")
                .append("\n")
                .append("extern int yylex(void);\n")
                .append("void yyparse(void);\n")
                .append("void do_action(int production_index);\n")
                .append("\n");

        generateData(source);

        source.append("void yyparse(void) {\n")
                .append("\tstd::stack<int> stack_state, stack_symbol;\n")
                .append("\tstack_state.push(0);\n")
                .append("\tint next = yylex();\n")
                .append("\tstd::cout << yytext << \": \" << symbols.find(next)->second << std::endl;\n")
                .append("\twhile (true) {\n")
                .append("\t\tstd::pair<int, int> key(stack_state.top(), next);\n")
                .append("\t\tstd::pair<int, int> action(-1, -1);\n")
                .append("\t\tauto action_iter = table_action.find(key);\n")
                .append("\t\tif (action_iter == table_action.end()) {\n")
                .append("\t\t\tstd::cerr << \"Error: unknown token\" << std::endl;\n")
                .append("\t\t} else {\n")
                .append("\t\t\taction = std::make_pair(action_iter -> second.first, action_iter -> second.second);\n")
                .append("\t\t}\n")
                .append("\t\tint head, body_size;\n")
                .append("\t\tstd::map<std::pair<int, int>, int>::iterator goto_iter;\n")
                .append("\t\tswitch (action.first) {\n")
                .append("\t\t\tcase SHIFT:\n")
                .append("\t\t\t\tstd::cout << \"\\tSHIFT\" << std::endl;\n")
                .append("\t\t\t\tstack_state.push(action.second);\n")
                .append("\t\t\t\tstack_symbol.push(next);\n")
                .append("\t\t\t\tnext = yylex();\n")
                .append("\t\t\t\tstd::cout << yytext << \": \" << symbols.find(next)->second << std::endl;\n")
                .append("\t\t\t\tbreak;\n")
                .append("\t\t\tcase REDUCE:\n")
                .append("\t\t\t\tstd::cout << \"\\tREDUCE\" << std::endl;\n")
                .append("\t\t\t\tdo_action(action.second);\n")
                .append("\t\t\t\thead = productions[action.second].first;\n")
                .append("\t\t\t\tbody_size = productions[action.second].second;\n")
                .append("\t\t\t\tfor (int i = 0; i < body_size; ++i) {\n")
                .append("\t\t\t\t\tstack_state.pop();\n")
                .append("\t\t\t\t\tstack_symbol.pop();\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t\tkey = std::make_pair(stack_state.top(), head);\n")
                .append("\t\t\t\tgoto_iter = table_goto.find(key);\n")
                .append("\t\t\t\tif (goto_iter == table_goto.end()) {\n")
                .append("\t\t\t\t\tstd::cerr << \"Error: Unknown token\" << std::endl;\n")
                .append("\t\t\t\t} else {\n")
                .append("\t\t\t\t\tstack_state.push(goto_iter -> second);\n")
                .append("\t\t\t\t\tstack_symbol.push(head);\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t\tbreak;\n")
                .append("\t\t\tcase ACCEPT:\n")
                .append("\t\t\t\tstd::cout << \"\\tACCEPT\" << std::endl;\n")
                .append("\t\t\t\treturn;\n")
                .append("\t\t\tdefault:\n")
                .append("\t\t\t\tstd::cerr << \"Error: Unknown action type\" << std::endl;\n")
                .append("\t\t\t\treturn;\n")
                .append("\t\t}\n")
                .append("\t}\n")
                .append("}\n")
                .append("\n")
                .append("void do_action(int production_index) {\n")
                .append("\tswitch (production_index) {\n");

        for (Production production : productions.getProductions()) {
            source.append("\t\tcase ").append(production.getIndex()).append(": {\n");
            if (production.getAction().length() == 0) {
                source.append("std::cout << \"\\t\\t");
                source.append(symbols.getInvertedSymbol(production.getHead())).append(" -> ");
                for (Integer i : production.getBody()) {
                    source.append(symbols.getInvertedSymbol(i)).append(" ");
                }
                source.append("\" << std::endl;\n");
            } else {
                source.append(production.getAction()).append("\n");
            }
            source.append("\t\t\tbreak;\n").append("\t\t}\n");
        }

        source.append("\t\tdefault:\n")
                .append("\t\t\tbreak;\n")
                .append("\t}\n")
                .append("}\n");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SOURCE_NAME))) {
            bw.write(source.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        System.out.println(source.toString());
    }

    private void generateData(StringBuilder source) {
        // goto
        source.append("std::map<std::pair<int, int>, int> table_goto = {\n");
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : tableGoto.entrySet()) {
            source.append("\t{{")
                    .append(entry.getKey().getKey().toString()).append(", ")
                    .append(entry.getKey().getValue().toString()).append("}, ")
                    .append(entry.getValue().toString()).append("},\n");
        }
        source.append("};\n");

        // action
        source.append("const int ACCEPT = 0, SHIFT = 1, REDUCE = 2;\n");
        final int ACCEPT = 0, SHIFT = 1, REDUCE = 2;
        source.append("std::map<std::pair<int, int>, std::pair<int, int>> table_action = {\n");
        for (Map.Entry<Pair<Integer, Integer>, Action> entry : tableAction.entrySet()) {
            Integer type = -1, target = -1;
            switch (entry.getValue().getType()) {
                case ACC:
                    type = ACCEPT;
                    target = -1;
                    break;
                case SHIFT:
                    type = SHIFT;
                    target = ((ShiftAction) entry.getValue()).getShiftTarget();
                    break;
                case REDUCE:
                    type = REDUCE;
                    target = ((ReduceAction) entry.getValue()).getProductionReducingBy();
            }
            source.append("\t{{")
                    .append(entry.getKey().getKey().toString()).append(", ")
                    .append(entry.getKey().getValue().toString()).append("}, {")
                    .append(type.toString()).append(", ")
                    .append(target.toString()).append("}},\n");
        }
        source.append("};\n\n");

        // productions
        source.append("std::map<int, std::pair<int, int>> productions = {\n");
        for (Production production : productions.getProductions()) {
            source.append("\t{").append(production.getIndex().toString())
                    .append(", {").append(production.getHead().toString())
                    .append(", ").append(String.valueOf(production.getBody().size()))
                    .append("}},\n");
        }
        source.append("};\n\n");

        // symbols
        source.append("std::map<int, std::string> symbols = {\n");
        for (Map.Entry<Integer, String> entry : symbols.getInvertedSymbols().entrySet()) {
            source.append("\t{").append(entry.getKey()).append(", \"").append(entry.getValue()).append("\"},\n");
        }
        source.append("};\n\n");
    }
}
