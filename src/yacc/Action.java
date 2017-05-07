package yacc;

enum ActionType {ACC, SHIFT, REDUCE}

class Action {
    private ActionType type;

    ActionType getType() {
        return type;
    }

    Action(ActionType type) {
        this.type = type;
    }


}

class AcceptAction extends Action {
    AcceptAction() {
        super(ActionType.ACC);
    }
}

class ShiftAction extends Action {
    private int shiftTarget;

    ShiftAction(int shiftTarget) {
        super(ActionType.SHIFT);
        this.shiftTarget = shiftTarget;
    }

    public int getShiftTarget() {
        return shiftTarget;
    }
}

class ReduceAction extends Action {
    private int productionReducingBy;

    public ReduceAction(int productionReducingBy) {
        super(ActionType.REDUCE);
        this.productionReducingBy = productionReducingBy;
    }

    public int getProductionReducingBy() {
        return productionReducingBy;
    }
}