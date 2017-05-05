package yacc;


import java.util.List;

class Production {
    private int head;
    private List<Integer> body;
    private int index;

    Production(int head, List<Integer> body, int index) {
        this.head = head;
        this.body = body;
        this.index = index;
    }

    int getHead() {
        return head;
    }

    int getIndex() {
        return index;
    }

    List<Integer> getBody() {
        return body;
    }
}
