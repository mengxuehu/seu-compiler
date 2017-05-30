package yacc.entity;

public class Associativity {
    private AssociativityType type;

    public Associativity(AssociativityType type) {
        this.type = type;
    }

    public boolean isLeftAssociativity() {
        return type.equals(AssociativityType.LEFT);
    }

    public boolean isRightAssociativity() {
        return type.equals(AssociativityType.RIGHT);
    }
}
