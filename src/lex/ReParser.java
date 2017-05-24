package lex;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class ReParser {

    public final static String epsilon = "\\~";
    private ArrayList<String> postfixRe = new ArrayList<>();

    String[] parse(String re) {
        postfixRe.clear();
        String mediumRe = processBrackets(re);
        mediumRe = processSign(mediumRe);
        mediumRe = addConnect(mediumRe);
        infixToPostfix(mediumRe);
        String[] returnString = new String[postfixRe.size()];
        for (int i = 0; i < returnString.length; i++) {
            returnString[i] = postfixRe.get(i);
        }
        return returnString;
    }

    private String processBrackets(String re) {
        //delete []
        StringBuilder regularRe = new StringBuilder();

        for (int i = 0; i < re.length(); i++) {
            if (re.charAt(i) == '[') {
                // nested []
                int times = 0;
                char pre = 0;
                regularRe.append('(');
                while (i + 1 < re.length() && re.charAt(++i) != ']') {
                    if (re.charAt(i) == '[')
                        times++;
                    else if (re.charAt(i) == '^') {
                        regularRe.append('!');
                        while (i + 1 < re.length() && re.charAt(++i) != ']') {
                            regularRe.append(re.charAt(i));
                        }
                        regularRe.append('|');
                        i--;
                    } else if (re.charAt(i) == '-') {
                        char post = re.charAt(i + 1);
                        if (post == ']' || post == '[') {
                            regularRe.append('\\');
                            regularRe.append(re.charAt(i));
                            regularRe.append('|');
                        } else {
                            for (char j = (char) (pre + 1); j <= post; j++) {
                                regularRe.append(j);
                                regularRe.append('|');
                            }
                            i++;
                        }

                    } else if (re.charAt(i) == '\\') { //solve transform meaning
                        regularRe.append(re.charAt(i));
                        regularRe.append(re.charAt(++i));
                        regularRe.append('|');
                    } else { // + * ? in the [] need to be transformed
                        if (re.charAt(i) == '+' || re.charAt(i) == '*'
                                || re.charAt(i) == '?') {
                            regularRe.append('\\');
                        }
                        pre = re.charAt(i);
                        regularRe.append(re.charAt(i));
                        regularRe.append('|');
                    }
                    // tell if solve all nested []
                    if (i + 1 < re.length() && times != 0 && re.charAt(i + 1) == ']') {
                        i++;
                        times--;
                    }
                }
                regularRe.deleteCharAt(regularRe.length() - 1);
                regularRe.append(')');
            } else if (re.charAt(i) == '?' || re.charAt(i) == '+' || re.charAt(i) == '*') {
                //sunion a serial of ? + *, but not transform to *
                char tempOp = re.charAt(i);
                while (i + 1 < re.length()) {
                    if (re.charAt(i + 1) == tempOp) {
                        i++;
                    } else if ((re.charAt(i + 1) == '?' && tempOp == '+')
                            || (re.charAt(i + 1) == '?' && tempOp == '*')
                            || (re.charAt(i + 1) == '+' && tempOp == '?')
                            || (re.charAt(i + 1) == '*' && tempOp == '?')
                            || (re.charAt(i + 1) == '+' && tempOp == '*')
                            || (re.charAt(i + 1) == '*' && tempOp == '+')) {
                        tempOp = '*';
                        i++;
                    } else {
                        break;
                    }
                }
                regularRe.append(tempOp);
            } else if (re.charAt(i) == '\\') { //solve transform meaning
                regularRe.append(re.charAt(i));
                regularRe.append(re.charAt(++i));
            } else { //normal solve
                regularRe.append(re.charAt(i));
            }
        }

        return regularRe.toString();
    }

    private String processSign(String mediumRe) {
        //delete ?, +, transform to *
        StringBuilder regularRe = new StringBuilder();
        StringBuilder tempRe = new StringBuilder();
        for (int i = 0; i < mediumRe.length(); i++) {
            if (mediumRe.charAt(i) == '\\') { //solve transform meaning
                regularRe.append(mediumRe.charAt(i));
                regularRe.append(mediumRe.charAt(++i));
            } else if (mediumRe.charAt(i) == '?' && mediumRe.charAt(i - 1) == ')') {
                //need to tell pre-char.if pre-char is ), need to solve the chars in the ()
                int length = regularRe.length() - 1, times = 0;
                while (regularRe.charAt(length) != '(') {
                    if (regularRe.charAt(length) == ')')
                        times++;
                    tempRe.append(regularRe.charAt(length));
                    regularRe.deleteCharAt(length);
                    length--;
                    if (regularRe.charAt(length) == '(' && times > 1) {
                        tempRe.append(regularRe.charAt(length));
                        regularRe.deleteCharAt(length);
                        length--;
                        times--;
                    }
                }
                regularRe.deleteCharAt(length);
                tempRe.append('(');
                tempRe.reverse();
                regularRe.append('(');
                regularRe.append(epsilon);
                regularRe.append('|');
                regularRe.append(tempRe.toString());
                regularRe.append(')');
                tempRe.delete(0, tempRe.length());
            } else if (mediumRe.charAt(i) == '?') {
                regularRe.deleteCharAt(regularRe.length() - 1);
                regularRe.append('(');
                regularRe.append(epsilon);
                regularRe.append('|');
                regularRe.append(mediumRe.charAt(i - 1));
                regularRe.append(')');
            } else if (mediumRe.charAt(i) == '+' && mediumRe.charAt(i - 1) == ')') {
                //method same as ?
                int length = regularRe.length() - 1, times = 0;
                while (regularRe.charAt(length) != '(') {
                    if (regularRe.charAt(length) == ')')
                        times++;
                    tempRe.append(regularRe.charAt(length));
                    regularRe.deleteCharAt(length);
                    length--;
                    if (regularRe.charAt(length) == '(' && times > 1) {
                        tempRe.append(regularRe.charAt(length));
                        regularRe.deleteCharAt(length);
                        length--;
                        times--;
                    }
                }
                regularRe.deleteCharAt(length);
                tempRe.append('(');
                tempRe.reverse();
                regularRe.append('(');
                regularRe.append(tempRe.toString());
                regularRe.append(tempRe.toString());
                regularRe.append(')');
                tempRe.delete(0, tempRe.length());
            } else if (mediumRe.charAt(i) == '+') {
                regularRe.deleteCharAt(regularRe.length() - 1);
                regularRe.append('(');
                regularRe.append(mediumRe.charAt(i - 1));
                regularRe.append(mediumRe.charAt(i - 1) + "*");
                regularRe.append(')');
            } else {
                regularRe.append(mediumRe.charAt(i));
            }
        }
        return regularRe.toString();
    }

    private String addConnect(String mediumRe) {
        //add connect symbol, because connect symbol is also a operate
        StringBuilder regularRe = new StringBuilder();
        int i = 1;
        char pre = mediumRe.charAt(0);
        regularRe.append(pre);
        if (pre == '\\') {
            regularRe.append(mediumRe.charAt(i++));
        }
        for (; i < mediumRe.length(); i++) {
            if (mediumRe.charAt(i) == '\\') {
                if (!(pre == '(' || pre == '|'))
                    regularRe.append('@');
                regularRe.append(mediumRe.charAt(i));
                regularRe.append(mediumRe.charAt(++i));
            } else if (mediumRe.charAt(i) == '!') {
                while (i + 1 < mediumRe.length() && mediumRe.charAt(i) != ')') {
                    regularRe.append(mediumRe.charAt(i));
                    i++;
                }
                i--;
            } else if (pre == '(' || mediumRe.charAt(i) == '*' || mediumRe.charAt(i) == '|'
                    || pre == '|' || mediumRe.charAt(i) == ')') {
                regularRe.append(mediumRe.charAt(i));
            } else {
                regularRe.append('@');
                regularRe.append(mediumRe.charAt(i));
            }
            pre = mediumRe.charAt(i);
        }
        return regularRe.toString();
    }

    private void infixToPostfix(String mediumRe) {
        Deque<String> op = new LinkedList<String>();
        Deque<String> identifier = new LinkedList<String>();

        for (int i = 0; i < mediumRe.length(); i++) {
            if (mediumRe.charAt(i) == '(') {
                op.push(String.valueOf(mediumRe.charAt(i)));
            } else if (mediumRe.charAt(i) == '*' || mediumRe.charAt(i) == '|' ||
                    mediumRe.charAt(i) == '@') {

                String opTemp = String.valueOf(mediumRe.charAt(i));
                if (!op.isEmpty() && op.peek().equals("(")) {
                    op.push(opTemp);
                } else if (!op.isEmpty() && (op.peek().compareTo(opTemp) <= 0)) {
                    if (op.peek().equals("*") && identifier.size() >= 1) {
                        String id1 = identifier.pollFirst();
                        if (id1 != null)
                            postfixRe.add(id1);
                    } else if (identifier.size() >= 2) {
                        String id1 = identifier.pollFirst();
                        String id2 = identifier.pollFirst();
                        if (id2 != null)
                            postfixRe.add(id2);
                        if (id1 != null)
                            postfixRe.add(id1);
                    } else {
                        System.out.println("stack error");
                    }
                    postfixRe.add(op.pollFirst());
                    identifier.push(null);
                    op.push(opTemp);
                } else {
                    op.push(String.valueOf(mediumRe.charAt(i)));
                }
            } else if (mediumRe.charAt(i) == ')') {
                while (!op.isEmpty() && !op.peek().equals("(")) {
                    if (op.peek().equals("*") && identifier.size() >= 1) {
                        String id1 = identifier.pollFirst();
                        if (id1 != null)
                            postfixRe.add(id1);
                    } else if (identifier.size() >= 2) {
                        String id1 = identifier.pollFirst();
                        String id2 = identifier.pollFirst();
                        if (id2 != null)
                            postfixRe.add(id2);
                        if (id1 != null)
                            postfixRe.add(id1);
                    } else {
                        System.out.println("stack error");
                    }
                    postfixRe.add(op.pollFirst());
                    identifier.push(null);
                }
                if (op.isEmpty()) {
                    System.out.println("op stack empty");
                } else {
                    op.pollFirst();
                }

            } else if (mediumRe.charAt(i) == '\\') {
                identifier.push(mediumRe.substring(i, i + 2));
                i++;
            } else if (mediumRe.charAt(i) == '!') {
                int j = i;
                while (mediumRe.charAt(++i) != ')') ;
                identifier.push(mediumRe.substring(j, i));
                i--;
            } else {
                identifier.push(String.valueOf(mediumRe.charAt(i)));
            }
        }
        while (!op.isEmpty()) {
            if (op.peek().equals("*") && identifier.size() >= 1) {
                String id1 = identifier.pollFirst();
                if (id1 != null)
                    postfixRe.add(id1);
            } else if (identifier.size() >= 2) {
                String id1 = identifier.pollFirst();
                String id2 = identifier.pollFirst();
                if (id2 != null)
                    postfixRe.add(id2);
                if (id1 != null)
                    postfixRe.add(id1);
            } else {
                System.out.println("op stack error");
            }
            postfixRe.add(op.pollFirst());
            identifier.push(null);
        }
        if (mediumRe.length() == 1) {
            postfixRe.add(mediumRe);
        } else if (mediumRe.length() == 2 && mediumRe.charAt(0) == '\\') {
            postfixRe.add(mediumRe);
        } else if (!(identifier.size() == 1 && identifier.peek() == null)) {
            System.out.println("id stack error");
        }
    }

    ArrayList<String> getpostfixRe() {
        return postfixRe;
    }

//    public static void main(String[] args) {
//        ReParser ReParser = new ReParser();
//        System.out.println("L?'(\\\\.|[^\\\\'\\n])+'");
//        ReParser.parse("L?'(\\\\.|[^\\\\'\\n])+'");
//    }
}
