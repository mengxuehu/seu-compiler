package yacc.lr;

import javafx.util.Pair;
import yacc.entity.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class ParserGenerator {
    private static final String HEADER_NAME = "y.tab.h", SOURCE_NAME = "y.tab.cpp";
    private Productions productions;
    private Symbols symbols;
    private String programs;
    private Map<Pair<Integer, Integer>, Integer> tableGoto;
    private Map<Pair<Integer, Integer>, Action> tableAction;

    public void generate(Productions productions, Symbols symbols, String programs,
                         Map<Integer, Precedence> precedence, Map<Integer, Associativity> associativity) {
        this.productions = productions;
        this.symbols = symbols;
        this.programs = programs;

        long t1 = System.currentTimeMillis();
        LR1 lr1 = new LR1();
        lr1.parse(productions, symbols, precedence, associativity);
        Map<Pair<Integer, Integer>, Integer> tableGotoLR1 = lr1.getTableGoto();
        Map<Pair<Integer, Integer>, Action> tableActionLR1 = lr1.getTableAction();
        Set<ItemSet> collection = lr1.getCollection();

        long t2 = System.currentTimeMillis();
        System.out.println("LR1: " + (t2 - t1));

        LALR1 lalr1 = new LALR1();
        lalr1.generateLALR1(collection, tableGotoLR1, tableActionLR1);
        tableGoto = lalr1.getTableGoto();
        tableAction = lalr1.getTableAction();

        long t3 = System.currentTimeMillis();
        System.out.println("LALR1: " + (t3 - t2));

        doGenerate();

        System.out.println("Generating Code: " + (System.currentTimeMillis() - t3));
    }

    private void doGenerate() {
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
        header.append("extern std::string yytext;\n");

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
                .append("\tstack_state.push(start_production);\n")
                .append("\twhile (true) {\n")
                .append("\t\tint next = yylex();\n")
                .append("\t\tstd::pair<int, int> key(stack_state.top(), next);\n")
                .append("\t\tstd::pair<int, int> action;\n")
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
                .append("\t\t\t\tstack_state.push(action.second);\n")
                .append("\t\t\t\tstack_symbol.push(next);\n")
                .append("\t\t\t\tnext = yylex();\n")
                .append("\t\t\t\tbreak;\n")
                .append("\t\t\tcase REDUCE:\n")
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
                .append("\t\t\t\treturn;\n")
                .append("\t\t\tdefault:\n")
                .append("\t\t\t\tstd::cerr << \"Error: Unknown action type\" << std::endl;\n")
                .append("\t\t}\n")
                .append("\t}\n")
                .append("}\n")
                .append("\n")
                .append("void do_action(int production_index) {\n")
                .append("\tswitch (production_index) {\n");

        for (Production production : productions.getProductions()) {
            source.append("\t\tcase ").append(production.getIndex()).append(": {\n");
            if (production.getAction().length() == 0) {
                source.append("std::cout << \"");
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

        source.append("int start_production = ").append(productions.getStart().getIndex().toString())
                .append(";\n\n");
    }
}
