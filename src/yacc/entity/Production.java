package yacc.entity;


import java.util.List;

public class Production {
    private Integer head;
    private List<Integer> body;
    private Integer index = null;
    private String action = null;

    public Production(int head, List<Integer> body, String action) {
        this.head = head;
        this.body = body;
        this.action = action;
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

    public String getAction() {
        return action;
    }
}
