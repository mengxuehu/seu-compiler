package yacc;

import javafx.util.Pair;
import yacc.util.YaccStringBuilder;

import java.util.Map;

class ParserSourceGenerator {
    private static final String HEADER_NAME = "yacc.tab.h", SOURCE_NAME = "yacc.tab.cpp";

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

        /* includes */
        source.appendLine("#include <map>")
                .appendLine("#include <stack>")
                .appendLine("#include <utility>")
                .appendLine("#include <iostream>")
                .appendBlankLine();

        /* user programs */
        source.appendLine(programs).appendBlankLine();

        /* parsing table */
        generateData(tableGoto, tableAction, productions, source);

        /* main, ... */
        source.appendLine("void yyparse(void);");
        source.setNumTabs(1);
        source.appendLine("int main() {")
                .appendLineWithTabs("yyparse();")
                .appendLineWithTabs("return 0;")
                .appendLine("}");
        source.decreaseNumTabs();

        /* yyparse() */
        source.appendLine("void yyparse(void) {");

        source.increaseNumTabs();

        source.appendLineWithTabs("stack<int> stack_state, stack_symbol;");
        source.appendWithTabs("stack_state.push(")
                .append(productions.getStart().getIndex().toString()).appendLine(");");

        // while begin
        source.appendLineWithTabs("while (true) {");
        source.increaseNumTabs();

        source.appendLineWithTabs("int next = yylex();");
        source.appendLineWithTabs("std::pair<int, int> key(stack_state.top(), next);");
        source.appendLineWithTabs("std::pair<int, int> action;");
        source.appendLineWithTabs("auto it = table_action.find(key);");

        // if begin
        source.appendLineWithTabs("if ( it == table_action.end() {");
        source.appendLineWithTabs("\tstd::cerr << \"Error: unknown token << std::endl;");
        source.appendLineWithTabs("} else {");
        source.appendLineWithTabs("\taction = std::make_pair(it -> first, it -> second);");
        source.appendLineWithTabs("}");
        // if end

        // switch begin
        String switchString = "switch (action.first) {\n" +
                "\tcase SHIFT:\n" +
                "\t\tstack_state.push(action.second);\n" +
                "\t\tstack_symbol.push(next);\n" +
                "\t\tnext = yylex();\n" +
                "\t\tbreak;\n" +
                "\tcase REDUCE:\n" +
                "\t\tint head = productions[action.second].first;\n" +
                "\t\tint body_size = productions[action.second].second;\n" +
                "\t\tfor (int i = 0; i < body_size; ++i) {\n" +
                "\t\t\tstack_state.pop();\n" +
                "\t\t\tstack_symbol.pop();\n" +
                "\t\t}\n" +
                "\t\tkey = std::make_pair(stack_stack.top(), head);\n" +
                "\t\tauto iter = table_goto.find(key);\n" +
                "\t\tif (key == table_goto.end()) {\n" +
                "\t\t\tstd::cerr << \"Error: Unknown token\" << std::endl;\n" +
                "\t\t} else {\n" +
                "\t\t\tstack_state.push(*iter);\n" +
                "\t\t\tstack_symbol.push(head);\n" +
                "\t\t}\n" +
                "\t\tbreak;\n" +
                "\tcase ACCEPT:\n" +
                "\t\treturn;\n" +
                "\t\tbreak;\n" +
                "\tdefault:\n" +
                "\t\tstd::cerr << \"Error: Unknown action type\" << std::endl;\n" +
                "}";

        while (source.getNumTabs() != 0) {
            source.decreaseNumTabs();
            source.appendLineWithTabs("}");
        }
        // all end
        System.out.println(source.toString());
    }

    private void generateData(Map<Pair<Integer, Integer>, Integer> tableGoto,
                              Map<Pair<Integer, Integer>, Action> tableAction,
                              Productions productions,
                              YaccStringBuilder source) {
        // goto
        source.appendLine("std::map<std::pair<int, int>, int> table_goto = {");
        for (Map.Entry<Pair<Integer, Integer>, Integer> entry : tableGoto.entrySet()) {
            source.append("\t{{")
                    .append(entry.getKey().getKey().toString()).append(", ")
                    .append(entry.getKey().getValue().toString()).append("}, ")
                    .append(entry.getValue().toString()).appendLine("},");
        }
        source.appendLine("};").appendBlankLine();

        // action
        source.appendLine("const int ACCEPT = 0, SHIFT = 1, REDUCE = 2;");
        final int ACCEPT = 0, SHIFT = 1, REDUCE = 2;
        source.appendLine("std::map<std::pair<int, int>, std::pair<int, int>> table_action = {");
        for (Map.Entry<Pair<Integer, Integer>, Action> entry : tableAction.entrySet()) {
            Integer type = -1, target = -1;
            switch (entry.getValue().getType()) {
                case ACC:
                    type = ACCEPT;
                    target = -1;
                    break;
                case SHIFT:
                    type = SHIFT;
                    target = ((ShiftAction)entry.getValue()).getShiftTarget();
                    break;
                case REDUCE:
                    type = REDUCE;
                    target = ((ReduceAction)entry.getValue()).getProductionReducingBy();
            }
            source.append("\t{{")
                    .append(entry.getKey().getKey().toString()).append(", ")
                    .append(entry.getKey().getValue().toString()).append("}, {")
                    .append(type.toString()).append(", ")
                    .append(target.toString()).appendLine("}},");
        }
        source.appendLine("};").appendBlankLine();

        // productions
        source.appendLine("std::map<int, std::pair<int, int>> productions = {");
        for (Production production : productions.getProductions()) {
            source.append("\t{").append(production.getIndex().toString())
                    .append(", {").append(production.getHead().toString())
                    .append(", ").append(String.valueOf(production.getBody().size()))
                    .appendLine("}}");
        }
        source.appendLine("};").appendBlankLine();

        source.append("start_production = ").append(productions.getStart().toString())
                .appendLine(";").appendBlankLine();
    }

    private void generateHeader(Symbols symbols) {
        YaccStringBuilder header = new YaccStringBuilder();
        for (Map.Entry<String, Integer> sym : symbols.getSymbols().entrySet()) {
            if (symbols.isTerminal(sym.getValue())
                    && (sym.getKey().length() != 1 || Character.isLetter(sym.getKey().charAt(0)))) {
                header.append("#define ").append(sym.getKey()).append(" ").appendLine(sym.getValue().toString());
            }
        }

        System.out.println(header.toString());
    }
}
