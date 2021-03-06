package yacc.lr;

enum ActionType {ACC, SHIFT, REDUCE}

class Action {
    private ActionType type;

    Action(ActionType type) {
        this.type = type;
    }

    ActionType getType() {
        return type;
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
    boolean setShiftTarget(int shiftTarget) {
        this.shiftTarget = shiftTarget;
        return true;
    }

    int getShiftTarget() {
        return shiftTarget;
    }
}

class ReduceAction extends Action {
    private int productionReducingBy;

    ReduceAction(int productionReducingBy) {
        super(ActionType.REDUCE);
        this.productionReducingBy = productionReducingBy;
    }

    int getProductionReducingBy() {
        return productionReducingBy;
    }
}