package yacc.entity;

public class Precedence implements Comparable<Precedence> {
    private Integer priority;

    public Precedence(Integer priority) {
        this.priority = priority;
    }

    @Override
    public int compareTo(Precedence o) {
        return Integer.compare(priority, o.priority);
    }
}
