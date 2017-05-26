package yacc.lr;

import javafx.util.Pair;
import yacc.entity.Production;
import yacc.entity.Productions;
import yacc.entity.Symbols;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ParserGenerator {
    private static final String HEADER_NAME = "yacc.tab.h", SOURCE_NAME = "yacc.tab.cpp";

    void generate(Productions productions, Symbols symbols, String programs) {
        LR1 lr1 = new LR1();
        lr1.parse(productions, symbols);
        Map<Pair<Integer, Integer>, Integer> tableGoto = lr1.getTableGoto();
        Map<Pair<Integer, Integer>, Action> tableAction = lr1.getTableAction();
        Set<ItemSet> collection = lr1.getCollection();

        LALR1 lalr1 = new LALR1();
        lalr1.generateLALR1(collection, tableGoto, tableAction);
        Map<Pair<Integer, Integer>, Integer> tableGotoLALR1 = lalr1.getTableGoto();
        Map<Pair<Integer, Integer>, Action> tableActionLALR1 = lalr1.getTableAction();

        doGenerate(tableGotoLALR1, tableActionLALR1, productions, symbols, programs);
    }

    private void doGenerate(Map<Pair<Integer, Integer>, Integer> tableGoto,
                            Map<Pair<Integer, Integer>, Action> tableAction,
                            Productions productions, Symbols symbols, String programs) {
        generateHeader(symbols);
        generateSource(tableGoto, tableAction, productions, programs);
    }

    private void generateHeader(Symbols symbols) {
        StringBuilder header = new StringBuilder();
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

    private void generateSource(Map<Pair<Integer, Integer>, Integer> tableGoto,
                                Map<Pair<Integer, Integer>, Action> tableAction,
                                Productions productions, String programs) {
        StringBuilder source = new StringBuilder();
        source.append("#include <map>\n")
                .append("#include <stack>\n")
                .append("#include <utility>\n")
                .append("#include <iostream>\n")
                .append("#include <string>\n")
                .append("\n")
                .append(programs)
                .append("\n\n")
                .append("void yyparse(void);\n")
                .append("int main() {\n")
                .append("\tyyparse();\n")
                .append("\treturn 0;\n")
                .append("}\n");
        generateData(tableGoto, tableAction, productions, source);

        source.append("void yyparse(void) {\n")
                .append("\tstd::stack<int> stack_state, stack_symbol;\n")
                .append("\tstack_state.push(start_production);\n")
                .append("\twhile (true) {\n")
                .append("\t\tint next = yylex();\n")
                .append("\t\tstd::pair<int, int> key(stack_state.top(), next);\n")
                .append("\t\tstd::pair<int, int> action;\n")
                .append("\t\tauto it = table_action.find(key);\n")
                .append("\t\tif (it == table_action.end()) {\n")
                .append("\t\t\tstd::cerr << \"Error: unknown token\" << std::endl;\n")
                .append("\t\t} else {\n")
                .append("\t\t\taction = std::make_pair(it ->second.first, it ->second.second);\n")
                .append("\t\t}\n")
                .append("\t\tint head, body_size;\n")
                .append("\t\tstd::map<std::pair<int, int>, int>::iterator iter;\n")
                .append("\t\tswitch (action.first) {\n")
                .append("\t\t\tcase SHIFT:\n")
                .append("\t\t\t\tstack_state.push(action.second);\n")
                .append("\t\t\t\tstack_symbol.push(next);\n")
                .append("\t\t\t\tnext = yylex();\n")
                .append("\t\t\t\tbreak;\n")
                .append("\t\t\tcase REDUCE:\n")
                .append("\t\t\t\thead = productions[action.second].first;\n")
                .append("\t\t\t\tbody_size = productions[action.second].second;\n")
                .append("\t\t\t\tfor (int i = 0; i < body_size; ++i) {\n")
                .append("\t\t\t\t\tstack_state.pop();\n")
                .append("\t\t\t\t\tstack_symbol.pop();\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t\tkey = std::make_pair(stack_state.top(), head);\n")
                .append("\t\t\t\titer = table_goto.find(key);\n")
                .append("\t\t\t\tif (iter == table_goto.end()) {\n")
                .append("\t\t\t\t\tstd::cerr << \"Error: Unknown token\" << std::endl;\n")
                .append("\t\t\t\t} else {\n")
                .append("\t\t\t\t\tstack_state.push(iter -> second);\n")
                .append("\t\t\t\t\tstack_symbol.push(head);\n")
                .append("\t\t\t\t}\n")
                .append("\t\t\t\tbreak;\n")
                .append("\t\t\tcase ACCEPT:\n")
                .append("\t\t\t\treturn;\n")
                .append("\t\t\tdefault:\n")
                .append("\t\t\t\tstd::cerr << \"Error: Unknown action type\" << std::endl;\n")
                .append("\t\t}\n")
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

    private void generateData(Map<Pair<Integer, Integer>, Integer> tableGoto,
                              Map<Pair<Integer, Integer>, Action> tableAction,
                              Productions productions,
                              StringBuilder source) {
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

        source.append("int start_production = ").append(productions.getStart().getIndex().toString())
                .append(";\n\n");
    }
}