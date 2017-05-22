package yacc;


import java.util.List;

class Production {
    private Integer head;
    private List<Integer> body;
    private Integer index = null;

    Production(int head, List<Integer> body) {
        this.head = head;
        this.body = body;
    }

    void setIndex(int index) {
        this.index = index;
    }

    Integer getHead() {
        return head;
    }

    Integer getIndex() {
        return index;
    }

    List<Integer> getBody() {
        return body;
    }
}
