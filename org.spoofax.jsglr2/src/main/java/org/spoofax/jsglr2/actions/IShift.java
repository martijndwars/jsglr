package org.spoofax.jsglr2.actions;

public interface IShift extends IAction {
    
    default public ActionType actionType() {
        return ActionType.SHIFT;
    }

    int shiftState();
    
}