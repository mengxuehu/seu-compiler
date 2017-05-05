package yacc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class YaccSourceParser {
    private static final int DECLARATIONS = 0, RULES = 1, PROGRAMS = 2;

    private StringBuilder programs;
    private Symbols symbols;
    private Integer start;

    private List<Production> productions;
    private List<String> productionActions;

    String getPrograms() {
        return programs.toString();
    }

    Symbols getSymbols() {
        return symbols;
    }

    Integer getStart() {
        return start;
    }

    List<Production> getProductions() {
        return productions;
    }

    List<String> getProductionActions() {
        return productionActions;
    }

    void parse(String sourcePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {
            int lineNumber = 0;
            int section = DECLARATIONS;
            start = null;

            programs = new StringBuilder();
            symbols = new Symbols();
            productions = new LinkedList<>();
            productionActions = new ArrayList<>();

            boolean inComment = false, inRule = false;
            StringBuilder tempRule = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                // section user programs
                if (section == PROGRAMS) {
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
                            productions.add(new Production(headIdx, symbolList, productionActions.size()));
                            if (ruleBody.length == 2 && ruleBody[1].matches("\\s*\\S.*")) {
                                productionActions.add(ruleBody[1]);
                            } else {
                                productionActions.add("");
                            }
                        }

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
                        if ((start = symbols.addNonTerminal(line.substring(7).trim())) == -1) {
                            start = null;
                            handleError("repeated declaration", lineNumber);
                        }
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

            System.out.println(programs.toString());

            System.out.println("------------------------------------------");

            for (String s : productionActions) {
                System.out.println(s);
            }

            System.out.println("------------------------------------------");
//
            for (Map.Entry<String, Integer> entry : symbols.getSymbols().entrySet()) {
                System.out.println(entry.getKey() + "----->" + entry.getValue());
            }

            System.out.println("------------------------------------------");

            for (Production production : productions) {
                System.out.println(production.getIndex());
                System.out.println(production.getHead());
                System.out.println(production.getBody());
                System.out.println();
            }

        } catch (FileNotFoundException e) {
            handleError("can't open file \"" + sourcePath + "\" to read", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleError(String errMsg, int lineNo) {
        System.err.println("ERROR(" + lineNo + "): " + errMsg);
        // exit is necessary, otherwise bugs will be introduced
        System.exit(1);
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.y").toString();
        YaccSourceParser lsp = new YaccSourceParser();
        lsp.parse(sourcePath);
    }
}
