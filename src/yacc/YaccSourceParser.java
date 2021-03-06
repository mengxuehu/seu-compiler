package yacc;

import yacc.entity.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class YaccSourceParser {
    private static final int DECLARATIONS = 0, RULES = 1, PROGRAMS = 2;

    private StringBuilder programs;
    private Symbols symbols;
    private Map<Integer, Precedence> precedence;
    private Map<Integer, Associativity> associativity;

    private Productions productions;

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.y").toString();
        YaccSourceParser lsp = new YaccSourceParser();
        lsp.parse(sourcePath);
    }

    Map<Integer, Precedence> getPrecedence() {
        return precedence;
    }

    Map<Integer, Associativity> getAssociativity() {
        return associativity;
    }

    void parse(String sourcePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {
            int lineNumber = 0;
            int section = DECLARATIONS;
            int currentPrecedence = 0;

            programs = new StringBuilder();
            symbols = new Symbols();
            productions = new Productions();

            precedence = new HashMap<>();
            associativity = new HashMap<>();

            boolean inComment = false, inRule = false;
            StringBuilder tempRule = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                // section user programs
                if (section == PROGRAMS) {
                    parseRule(line, tempRule, lineNumber);
                    break;
                }

                // empty line
                if (line.isEmpty()) {
                    continue;
                }

                if (inComment) {
                    if (line.startsWith("%}")) {
                        inComment = false;
                    }
                    // multi-line comment ends
                    else {
                        programs.append(line).append('\n');
                    }
                    continue;
                }
                // multi-line comment begins
                else if (line.startsWith("%{")) {
                    inComment = true;
                    continue;
                }
                // section delimiter
                if (line.startsWith("%%")) {
                    ++section;
                    continue;
                }

                // in rule definition
                if (inRule) {
                    if (Character.isWhitespace(line.charAt(0))) {
                        tempRule.append(line).append("\n");
                        continue;
                    } else {
                        parseRule(line, tempRule, lineNumber);
                        inRule = false;
                    }
                }

                if (section == DECLARATIONS) {
                    if (line.startsWith("%token ")) {
                        String[] decs = line.substring(7).trim().split(" ");
                        for (String dec : decs) {
                            if (symbols.addTerminal(dec) == -1) {
                                handleError("repeated declaration", lineNumber);
                            }
                            symbols.addTerminal(dec);
                        }
                    } else if (line.startsWith("%start ")) {
                        int start = symbols.addNonTerminal(line.substring(7).trim());
                        if (start == -1) {
                            handleError("repeated declaration", lineNumber);
                        } else {
                            List<Integer> symbolList = new LinkedList<>();
                            symbolList.add(start);
                            productions.addAugmentedStartAndSetIndex(
                                    new Production(symbols.getStartAug(), symbolList, ""));
                        }
                    } else if (line.startsWith("%left") || line.startsWith("%right")) {
                        boolean left = (line.charAt(1) == 'l');
                        String [] operators = line.substring(left ? 5 : 6).trim().split("[\t ]");
                        for (String operator : operators) {
                            Character ch = (operator.charAt(0) == '\'' ? operator.charAt(1) : null);

                            int index = (ch != null ? symbols.addChar(ch) : symbols.addTerminal(operator));
                            if (index == -1) {
                                index = symbols.getSymbolIndex(ch != null ? String.valueOf(ch) : operator);
                            }
                            precedence.put(index, new Precedence(currentPrecedence));
                            associativity.put(index,
                                    new Associativity(left ? AssociativityType.LEFT : AssociativityType.RIGHT));
                        }
                        currentPrecedence++;
                    }
                } else if (section == RULES) {
                    inRule = true;
                    tempRule.setLength(0);
                    tempRule.append(line).append("\n");
                } else {
                    handleError("unexpected error", lineNumber);
                }
            }

            /* section programs, copy all lines left to target file. */
            while (line != null) {
                programs.append(line).append('\n');
                line = br.readLine();
                ++lineNumber;
            }

//            System.out.println(programs.toString());
//
//            System.out.println("------------------------------------------");
//
//            for (String s : productions.getActions()) {
//                System.out.println(s);
//            }
//
//            System.out.println("------------------------------------------");
////
//            for (Map.Entry<String, Integer> entry : symbols.getSymbols().entrySet()) {
//                System.out.println(entry.getKey() + "----->" + entry.getValue());
//            }
//
//            System.out.println("------------------------------------------");
//
//            for (Production production : productions.getProductions()) {
//                System.out.println(production.getIndex());
//                System.out.println(production.getHead());
//                System.out.println(production.getBody());
//                System.out.println();
//            }

        } catch (FileNotFoundException e) {
            handleError("can't open file \"" + sourcePath + "\" to read", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRule(String line, StringBuilder tempRule, int lineNumber) {
        String[] ruleLines = tempRule.toString().split("[\t\n ]+:[\t\n ]+", 2);
        if (ruleLines.length != 2) {
            handleError("invalid rule", lineNumber - 1);
        }

        String[] ruleHead = ruleLines[0].split("[\t\n ]+", 2);
        if (ruleHead.length != 1) {
            handleError("invalid rule", lineNumber - 1);
        }
        int headIdx;
        if ((headIdx = symbols.addNonTerminal(ruleHead[0].trim())) == -1) {
            headIdx = symbols.getSymbolIndex(ruleHead[0].trim());
        }

        int posSemicolon = ruleLines[1].lastIndexOf(";");
        if (posSemicolon == -1) {
            handleError("invalid rule", lineNumber - 1);
        }
        String ruleBodys[] = ruleLines[1].substring(0, posSemicolon).split("\\s+\\|\\s+");
        for (String s : ruleBodys) {
            String[] ruleBody = s.replaceFirst("^\\s+", "")
                    .replaceFirst("\\s+$", "").split("\t+", 2);
            if (ruleBody.length > 2) {
                handleError("invalid rule", lineNumber - 1);
            }

            String[] ruleBodyLeft = ruleBody[0].split(" +");
            List<Integer> symbolList = new LinkedList<>();
            for (String i : ruleBodyLeft) {
                if (i.isEmpty()) {
                    continue;
                }
                if (i.charAt(0) == '\'') {
                    if (i.length() != 3 || i.charAt(2) != '\'') {
                        handleError("invalid rule", lineNumber - 1);
                    }
                    String temp = String.valueOf(i.charAt(1));
                    symbolList.add(symbols.contains(temp) ?
                            symbols.getSymbolIndex(temp) : symbols.addChar(i.charAt(1)));
                } else {
                    symbolList.add(symbols.contains(i) ?
                            symbols.getSymbolIndex(i) : symbols.addNonTerminal(i));
                }
            }
            if (ruleBody.length == 2 && ruleBody[1].matches("\\s*\\S.*")) {
                productions.addProductionAndSetIndex(new Production(headIdx, symbolList, ruleBody[1]));
            } else {
                productions.addProductionAndSetIndex(new Production(headIdx, symbolList, ""));
            }
        }
    }

//    List<String> getProductionActions() {
//        return productionActions;
//    }

    private void handleError(String errMsg, int lineNo) {
        System.err.println("ERROR(" + lineNo + "): " + errMsg);
        // exit is necessary, otherwise bugs will be introduced
        System.exit(1);
    }

    String getPrograms() {
        return programs.toString();
    }

    Symbols getSymbols() {
        return symbols;
    }

    Productions getProductions() {
        return productions;
    }
}
