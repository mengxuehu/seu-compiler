package lex;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public class ReParser {
	
	public final static String epsilon = "\\~";
	private ArrayList<String> postfixRe = new ArrayList<>();
	
    private String processBrackets(String re) {
    	//delete []
        StringBuilder regularRe = new StringBuilder();
        
		for (int i = 0; i < re.length(); i++) {
//	        	if (re.charAt(i) == '{') {
//	        		StringBuilder def = new StringBuilder();
//	            	while(re.charAt(++i) != '}') {
//	        			def.append(re.charAt(i));
//	        		}
//	        		regularRe.append(preprocessRe(find(def.toString())));
//				} else 
			if (re.charAt(i) == '[') {
				// nested []
				int times = 0;
				char pre = 0;
				regularRe.append('(');
				while(re.charAt(++i) != ']') {
					if (re.charAt(i) == '[')
						times++;
					else if (re.charAt(i) == '^') 
						regularRe.append('!');
					else if (re.charAt(i) == '-'){
						char post = re.charAt(++i);
						for (char j = (char) (pre+1); j <= post; j++) {
							regularRe.append(j);
							regularRe.append('|');
						}
					} else if (re.charAt(i) == '\\') {
						regularRe.append(re.charAt(i));
						regularRe.append(re.charAt(++i));
						regularRe.append('|');
					} else {
						pre = re.charAt(i);
						regularRe.append(re.charAt(i));
						regularRe.append('|');
					}
					
					if (times != 0 && re.charAt(i+1) == ']') {
						i++;
						times--;
					}
        		}
				regularRe.deleteCharAt(regularRe.length()-1);
				regularRe.append(')');
			} 
//					else if (re.charAt(i) == '"') {
//					if (re.charAt(i+1) == '.') {
//						regularRe.append('\\');
//						regularRe.append(re.charAt(++i));
//						++i;
//					} else {
//						regularRe.append(re.charAt(i));
//					}
//				} 
//				  else if (re.charAt(i) == '!') {
//					regularRe.append('\\');
//					regularRe.append('!');
//				} 
			  else if (re.charAt(i) == '?' || re.charAt(i) == '+' || re.charAt(i) == '*') {
				char tempOp = re.charAt(i);
				while(i+1 < re.length()){
					if (re.charAt(i+1) == tempOp){
						i++;
					} else if ((re.charAt(i+1) == '?'&& tempOp == '+')
								|| (re.charAt(i+1) == '?'&& tempOp == '*')
								|| (re.charAt(i+1) == '+'&& tempOp == '?')
								|| (re.charAt(i+1) == '*'&& tempOp == '?')
								|| (re.charAt(i+1) == '+'&& tempOp == '*')
								||  (re.charAt(i+1) == '*'&& tempOp == '+')) {
						tempOp = '*';
						i++;
					} else {
						break;
					}
				}
				regularRe.append(tempOp);
			} else if (re.charAt(i) == '\\') {
				regularRe.append(re.charAt(i));
				regularRe.append(re.charAt(++i));
			} else {
				regularRe.append(re.charAt(i));
			}
        }
        
        return regularRe.toString();
    }
    
    private String processSign(String mediumRe) {
    	//delete ?, +
    	StringBuilder regularRe = new StringBuilder();
    	StringBuilder tempRe = new StringBuilder();
    	for (int i = 0; i < mediumRe.length(); i++) {
    		if (mediumRe.charAt(i) == '\\') {
				regularRe.append(mediumRe.charAt(i));
				regularRe.append(mediumRe.charAt(++i));
			} else if (mediumRe.charAt(i) == '?' && mediumRe.charAt(i-1) == ')') {
				int j = i-1;
				int length = regularRe.length() - 1;
				while (mediumRe.charAt(j) != '(') {
					tempRe.append(mediumRe.charAt(j));
					regularRe.deleteCharAt(length);
					j--;
					length--;
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
				regularRe.append('(');
				regularRe.append(epsilon);
				regularRe.append('|');
				regularRe.append(mediumRe.charAt(i-1));
				regularRe.append(')');
			} else if (mediumRe.charAt(i) == '+' && mediumRe.charAt(i-1) == ')') {
				int j = i-1;
				int length = regularRe.length() - 1;
				while (mediumRe.charAt(j) != '(') {
					tempRe.append(mediumRe.charAt(j));
					regularRe.deleteCharAt(length);
					j--;
					length--;
				}
				regularRe.deleteCharAt(length);
				tempRe.append('(');
				tempRe.reverse();
				regularRe.append('(');
				regularRe.append(tempRe.toString());
				regularRe.append(tempRe.toString()+"*");
				regularRe.append(')');
				tempRe.delete(0, tempRe.length());
			} else if (mediumRe.charAt(i) == '+') {
				regularRe.append('(');
				regularRe.append(mediumRe.charAt(i-1));
				regularRe.append(mediumRe.charAt(i-1)+"*");
				regularRe.append(')');
			} else {
				regularRe.append(mediumRe.charAt(i));
			}
		}
		return regularRe.toString();
	}
    
    private String addConnect(String mediumRe) {
    	StringBuilder regularRe = new StringBuilder();
    	char pre = mediumRe.charAt(0);
    	regularRe.append(pre);
    	for (int i = 1; i < mediumRe.length(); i++) {
			if (pre == '(' || mediumRe.charAt(i) == '*' || mediumRe.charAt(i) == '|'
						|| pre =='|' || mediumRe.charAt(i) == ')' || pre == '\\' || pre == '!') {
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
						System.err.println("stack error");
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
						System.err.println("stack error");
					}
					postfixRe.add(op.pollFirst());
					identifier.push(null);
					
				}
				if (op.isEmpty()) {
					System.err.println("op stack empty");
				} else {
					op.pollFirst();
				}
				
			} else if (mediumRe.charAt(i) == '\\' || mediumRe.charAt(i) == '!') {
				StringBuilder temp = new StringBuilder();
				temp.append(mediumRe.charAt(i));
				temp.append(mediumRe.charAt(++i));
				identifier.push(temp.toString());
			} else {
				identifier.push(String.valueOf(mediumRe.charAt(i)));
			}
		}
    	while(!op.isEmpty()){
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
				System.err.println("op stack error");
			}
			postfixRe.add(op.pollFirst());
			identifier.push(null);
    	}
    	if (!(identifier.size() == 1 && identifier.peek() == null)) {
    		System.err.println("id stack error");
		}
    }
    
    public String[] processRe(String re) {
    	postfixRe.clear();
		String mediumRe = processBrackets(re);
		System.out.println(mediumRe);
		mediumRe = processSign(mediumRe);
		System.out.println(mediumRe);
		mediumRe = addConnect(mediumRe);
		System.out.println(mediumRe);
		infixToPostfix(mediumRe);
		for (int i = 0; i < postfixRe.size(); i++) {
			System.out.println(postfixRe.get(i));
		}
		String[] returnString = new String[postfixRe.size()];
		for (int i = 0; i < returnString.length; i++) {
			returnString[i] = postfixRe.get(i);
		}
		return returnString;
	}
    

	public ArrayList<String> getpostfixRe() {
		return postfixRe;
	}
    
    public static void main(String[] args) {
        ReParser ReParser = new ReParser();
        ReParser.processRe("([a-c[d-e]f_])?*[\\+]?");
    }
}
