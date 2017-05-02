package yacc;


import java.util.List;

class Production {
    private int head;
    private List<Integer> body;

    Production(int head, List<Integer> body) {
        this.head = head;
        this.body = body;
    }

    int getHead() {
        return head;
    }

    List<Integer> getBody() {
        return body;
    }
}
