package lex;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

class LexSourceParser {
    private static final int DEFINITIONS = 1, RULES = 2, USER_ROUTINES = 3;
    private static final String escapeChars = "\\.()[]+-*|@?!";  // ^$

    private int lineNumber = 0;
    private int section;

    // definitions in section DEFINITIONS
    private Map<String, String> definitions;
    private Map<String, String> rules;
    private StringBuilder userRoutines;

    LexSourceParser() {
    }

    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.l").toString();
        LexSourceParser lsp = new LexSourceParser();
        lsp.parse(sourcePath);
    }

    void parse(String sourcePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {
            definitions = new HashMap<>();
            rules = new LinkedHashMap<>();
            userRoutines = new StringBuilder();

            section = DEFINITIONS;
            boolean inComment = false, inRule = false;

            // used in SECTION RULES
            StringBuilder ruleAction = new StringBuilder();
            String ruleDef = "";

            // one line of source
            String line;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                // section user routines
                if (section == USER_ROUTINES) {
                    parseRule(ruleAction, ruleDef);
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
                        userRoutines.append(line).append('\n');
                    }
                    continue;
                }
                // multi-line comment begins
                else if (line.startsWith("%{")) {
                    inComment = true;
                    continue;
                }
                // section delimiter
                else if (line.startsWith("%%")) {
                    ++section;
                    continue;
                }

                // in rule definition
                if (inRule) {
                    if (isBlank(line.charAt(0))) {
                        ruleAction.append(line).append('\n');
                        continue;
                    } else {
                        parseRule(ruleAction, ruleDef);
                        inRule = false;
                    }
                }
                // single line comment
                else if (line.startsWith(" ") || line.startsWith("\t")) {
                    userRoutines.append(line.trim()).append('\n');
                    continue;
                }

                // section definitions
                String[] pair = splitByTab(line);
                if (section == DEFINITIONS) {
                    if (pair == null) {
                        handleError("invalid definition", lineNumber);
                    } else {
                        String right = toRe(pair[1]);
                        definitions.put(pair[0], right);
                    }
                }


                if (section == RULES) {
                    if (pair == null) {
                        handleError("invalid rules", lineNumber);
                    } else {
                        inRule = true;
                        ruleDef = toRe(pair[0]);
                        ruleAction = new StringBuilder(pair[1]);
                    }
                }

            }
            /* section user routines, copy all lines left to target file. */
            while (line != null) {
                userRoutines.append(line).append('\n');
                line = br.readLine();
                ++lineNumber;
            }
//            ReParser reParser = new ReParser();
//            for (String s : rules.keySet()) {
//                System.out.println();
//                System.out.println("\t" + s);
//                reParser.parse(s);
//            }

//            int i = 0;
//            for (String s : rules.keySet()) {
//                assert i < rules.size();
//                if (i == 0) {
//                    System.out.println();
//                    System.out.println("\t" + s);
//                    reParser.parse(s);
//                    break;
//                }
//                i--;
//            }
//            for (String s : definitions.keySet()) {
//                System.out.println(s + "->" + definitions.get(s));
//            }
//
//            for (String s : rules.keySet()) {
//                System.out.println(s + "->" + rules.get(s));
//            }
//
//            System.out.println(userRoutines.toString());
        } catch (FileNotFoundException e) {
            handleError("can't open file \"" + sourcePath + "\" to read", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseRule(StringBuilder ruleAction, String ruleDef) {
        if (ruleAction.toString().isEmpty()) {
            handleError("null rule action", lineNumber - 1);
        }
        rules.put(ruleDef, ruleAction.toString());
    }

    private boolean isBlank(char c) {
        return c == '\t' || c == ' ';
    }

    private String[] splitByTab(String line) {
        int i = line.indexOf('\t');
        if (i != -1) {
            String right = line.substring(i).replaceFirst("\t*", "");
            if (!right.isEmpty()) {
                return new String[]{line.substring(0, i), right};
            }
        }
        return null;
    }

    private void handleError(String errMsg, int lineNo) {
        System.err.println("ERROR(" + lineNo + "): " + errMsg);
        // exit is necessary, otherwise bugs will be introduced
        System.exit(1);
    }

    private String toRe(String s) {
        // whether last character is \
        boolean lastIsBackslash = false;

        for (int i = 0; i < s.length(); i++) {
            if (!lastIsBackslash) {
                int posRightMatch = i;
                switch (s.charAt(i)) {
                    case '[':
                        do {
                            posRightMatch = s.indexOf(']', posRightMatch + 1);
                        } while (s.charAt(posRightMatch - 1) == '\\');
                        if (posRightMatch != -1) {
                            i = posRightMatch;
                        } else {
                            handleError("mismatched brackets", lineNumber);
                        }
                        break;
                    case '"':
                        do {
                            posRightMatch = s.indexOf('"', posRightMatch + 1);
                        } while (s.charAt(posRightMatch - 1) == '\\');
                        if (posRightMatch != -1) {
                            String re = replaceQuotesWithRe(s.substring(i + 1, posRightMatch));
                            s = s.substring(0, i) + re + s.substring(posRightMatch + 1);
                            i = i + re.length() - 1;
                        } else {
                            handleError("mismatched quotes", lineNumber);
                        }
                        break;
                    case '{':
                        posRightMatch = s.indexOf('}', posRightMatch + 1);
                        if (posRightMatch != -1) {
                            String body = definitions.get(s.substring(i + 1, posRightMatch));
                            if (!body.isEmpty()) {
                                s = s.substring(0, i) + body + s.substring(posRightMatch + 1);
                                i = i + body.length() - 1;
                            } else {
                                handleError("invalid " + (section == DEFINITIONS ? "definition" : "rule"),
                                        lineNumber);
                            }
                        } else {
                            handleError("mismatched quotes", lineNumber);
                        }
                        break;
                    default:
                        break;
                }
            }
            lastIsBackslash = (s.charAt(i) == '\\');
        }
        return s;
    }

    private String replaceQuotesWithRe(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\\') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '"') {
                    s = s.substring(0, i) + s.substring(i + 1);
                    continue;
                }
            }
            if (escapeChars.contains(String.valueOf(s.charAt(i)))) {
                s = s.substring(0, i) + "\\" + s.substring(i++);
            }
        }
        return s;
    }

    Map<String, String> getRules() {
        return rules;
    }

    String getUserRoutines() {
        return userRoutines == null ? null : userRoutines.toString();
    }
}