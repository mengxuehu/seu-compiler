package yacc;


class Item {
    private int productionIndex;
    private int position;
//    private Set<Integer> first;

    Item(int productionIndex, int position) {
        this.productionIndex = productionIndex;
        this.position = position;
    }

    int getProductionIndex() {
        return productionIndex;
    }

    int getPosition() {
        return position;
    }
}
