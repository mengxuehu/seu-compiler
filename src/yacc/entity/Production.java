package yacc.entity;


import java.util.List;

public class Production {
    private Integer head;
    private List<Integer> body;
    private Integer index = null;

    public Production(int head, List<Integer> body) {
        this.head = head;
        this.body = body;
    }

    public Integer getHead() {
        return head;
    }

    public Integer getIndex() {
        return index;
    }

    void setIndex(int index) {
        this.index = index;
    }

    public List<Integer> getBody() {
        return body;
    }
}
