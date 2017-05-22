package yacc.util;


public class YaccStringBuilder {
    private StringBuilder sb;
    private int numTabs = 0;

    public YaccStringBuilder() {
        sb = new StringBuilder();
    }

    public YaccStringBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public YaccStringBuilder appendWithTabs(String s) {
        for (int i = 0; i < numTabs; i++) {
            sb.append('\t');
        }
        sb.append(s);
        return this;
    }

    public YaccStringBuilder appendLine(String s) {
        sb.append(s).append('\n');
        return this;
    }

//    public YaccStringBuilder appendLineWithTabs(String s) {
//        return this.appendWithTabs(s).appendBlankLine();
//    }

    public YaccStringBuilder appendBlankLine() {
        sb.append('\n');
        return this;
    }

//    public YaccStringBuilder insertFront(String s) {
//        sb.insert(0, s);
//        return this;
//    }

//    public YaccStringBuilder insertFrontLine(String s) {
//        sb.insert(0, '\n').insert(0, s);
//        return this;
//    }

//    public YaccStringBuilder insertFrontLineWithTabs(String s, int numTab) {
//        this.insertFrontLine(s);
//        for (int i = 0; i < numTab; i++) {
//            sb.insert(0, '\t');
//        }
//        return this;
//    }

//    public void increaseNumTabs() {
//        ++numTabs;
//    }
//
//    public void decreaseNumTabs() {
//        --numTabs;
//    }
//
//    public void setNumTabs(int numTabs) {
//        this.numTabs = numTabs;
//    }
//
//    public void clearNumTabs(int numTabs) {
//        this.numTabs = 0;
//    }
//
//    public int getNumTabs() {
//        return numTabs;
//    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
