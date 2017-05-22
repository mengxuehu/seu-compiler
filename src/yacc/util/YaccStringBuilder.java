package yacc.util;


public class YaccStringBuilder {
    private StringBuilder sb;

    public YaccStringBuilder() {
        sb = new StringBuilder();
    }

    public YaccStringBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public YaccStringBuilder appendLine(String s) {
        sb.append(s).append('\n');
        return this;
    }

    public YaccStringBuilder appendLineWithTabs(String s, int numTab) {
        for (int i = 0; i < numTab; i++) {
            sb.append('\t');
        }
        return this.appendLine(s);
    }

    public YaccStringBuilder insertFront(String s) {
        sb.insert(0, s);
        return this;
    }

    public YaccStringBuilder insertFrontLine(String s) {
        sb.insert(0, '\n').insert(0, s);
        return this;
    }

    public YaccStringBuilder insertFrontLineWithTabs(String s, int numTab) {
        this.insertFrontLine(s);
        for (int i = 0; i < numTab; i++) {
            sb.insert(0, '\t');
        }
        return this;
    }
}
