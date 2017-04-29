package lex;


import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LexSourceParser {
    private static final int DEFINITIONS = 1, RULES = 2, USER_ROUTINES = 3;
    private static final String escapeChars = "\\.()[]+-*|@?!";  // ^$

    private int lineNumber = 0;

    // definitions in section DEFINITIONS
    private Map<String, String> definitions = new HashMap<>();

    LexSourceParser(String sourcePath) {

        try (BufferedReader br = new BufferedReader(new FileReader(sourcePath))) {

            StringBuilder target = new StringBuilder();
            Map<String, String> rules = new LinkedHashMap<>();

            int section = DEFINITIONS;
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
                    break;
                }

                // empty line
                if (line.isEmpty()) {
                    continue;
                }

                if (inComment) {
                    // copy comment to target file
                    if (line.startsWith("%}")) {
                        inComment = false;
                    }
                    // multi-line comment ends
                    else {
                        target.append(line).append('\n');
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
                        if (ruleAction.toString().isEmpty()) {
                            int tmp = lineNumber - 1;
                            handleError("null rule action in line " + tmp);
                        }
                        rules.put(ruleDef, ruleAction.toString());
                        inRule = false;
                    }
                }
                // single line comment
                else if (line.startsWith(" ") || line.startsWith("\t")) {
                    target.append(line.trim()).append('\n');
                    continue;
                }

                // section definitions
                if (section == DEFINITIONS) {
                    boolean inSpace = false;
                    int defKeyEnd = 0, defValBegin = 0;
                    for (int i = 0; i < line.length(); i++) {
                        if (!inSpace && isBlank(line.charAt(i))) {
                            inSpace = true;
                            defKeyEnd = i;
                        } else if (inSpace && !isBlank(line.charAt(i))) {
                            defValBegin = i;
                            break;
                        }
                    }
                    if (!inSpace || defKeyEnd == 0 || defValBegin == 0) {
                        handleError("invalid definition in line " + lineNumber);
                    }
                    // replace old key if it exists
                    definitions.put(line.substring(0, defKeyEnd), replaceDefHeadWithBody(line.substring(defValBegin)));
                }

                // section RULES
                if (section == RULES) {
                    inRule = true;
                    ruleAction.setLength(0);

                    // end of rule definition
                    int ruleDefEnd = 0;

                    // position of left quote in line
                    int quoteBegin = -1;

                    // number of brackets mismatched
                    int numBracket = 0;

                    // whether last character is \
                    boolean lastIsBackslash = false;

                    // whether in quotes
                    boolean inQuotes = false;

                    for (int i = 0; i < line.length(); i++) {
                        if (inQuotes) {
                            if (line.charAt(i) == '"') {
                                inQuotes = false;
                                String re = replaceQuotesWithRE(line.substring(quoteBegin + 1, i));
                                line = line.substring(0, quoteBegin) + re + line.substring(i + 1);
                                i = quoteBegin + re.length() - 1;
                                quoteBegin = -1;
                            }
                        } else {
                            if (!lastIsBackslash) {
                                switch (line.charAt(i)) {
                                    case '[':
                                        numBracket += 1;
                                        break;
                                    case ']':
                                        if (numBracket > 0) {
                                            numBracket -= 1;
                                        } else {
                                            handleError("mismatched brackets in line " + lineNumber);
                                        }
                                        break;
                                    case '"':
                                        inQuotes = true;
                                        quoteBegin = i;
                                        break;
                                    case '{':
                                        for (int j = i + 1; j < line.length(); j++) {
                                            if (line.charAt(j) == '}') {
                                                String body = definitions.get(line.substring(i + 1, j));
                                                if (body.isEmpty()) {
                                                    handleError("invalid definition in line " + lineNumber);
                                                } else {
                                                    line = line.substring(0, i) + body + line.substring(j + 1);
                                                    i = i + body.length() - 1;
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if ((line.charAt(i) == '\t' || (line.charAt(i) == ' ')) && numBracket == 0) {
                                ruleDefEnd = i;
                                break;
                            }
                            lastIsBackslash = (line.charAt(i) == '\\');
                        }
                    }

                    if (ruleDefEnd == 0) {
                        handleError("invalid rule in line " + lineNumber);
                    } else {
                        ruleDef = line.substring(0, ruleDefEnd);
                    }

                    for (int i = ruleDefEnd; i < line.length(); i++) {
                        if (!isBlank(line.charAt(i))) {
                            ruleAction.append(line.substring(i));
                            break;
                        }
                    }
                }
            }

            /* section user routines, copy all lines left to target file. */
            while (line != null) {
                target.append(line).append('\n');
                line = br.readLine();
                ++lineNumber;
            }

            for (String s : definitions.keySet()) {
                System.out.println(s + "->" + definitions.get(s));
            }

            for (String s : rules.keySet()) {
                System.out.println(s + "->" + rules.get(s));
            }

            System.out.println(target.toString());
        } catch (FileNotFoundException e) {
            handleError("can't open file \"" + sourcePath + "\" to read");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isBlank(char c) {
        return c == '\t' || c == ' ';
    }

    private String replaceDefHeadWithBody(String s) {
        // whether last character is \
        boolean lastIsBackslash = false;
        for (int i = 0; i < s.length(); i++) {
            if (!lastIsBackslash && s.charAt(i) == '{') {
                for (int j = i + 1; j < s.length(); j++) {
                    if (s.charAt(j) == '}') {
                        String body = definitions.get(s.substring(i + 1, j));
                        if (body.isEmpty()) {
                            handleError("invalid definition in line " + lineNumber);
                        } else {
                            // size of s is changed
                            s = s.substring(0, i) + body + s.substring(j + 1);
                            i = i + body.length() - 1;
                        }
                        break;
                    }
                }
            }
            lastIsBackslash = (s.charAt(i) == '\\');
        }
        return s;
    }

    private String replaceQuotesWithRE(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (escapeChars.contains(String.valueOf(s.charAt(i)))) {
                s = s.substring(0, i) + "\\" + s.substring(i++);
            }
        }
        return s;
    }


    private void handleError(String errMsg) {
        System.err.println("ERROR: " + errMsg);
        // exit is necessary, otherwise bugs will be introduced
        System.exit(1);
    }


    public static void main(String[] args) {
        String sourcePath = Paths.get(System.getProperty("user.dir"), "c99.l").toString();
        new LexSourceParser(sourcePath);
    }
}