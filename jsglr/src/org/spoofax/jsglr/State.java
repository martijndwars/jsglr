/*
 * Created on 04.des.2005
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr;

import java.util.List;
import java.util.Vector;

public class State {

    private final int stateNumber;
    private final List<Goto> gotos;
    private final List<Action> actions;
    
    public State(int stateNumber, List<Goto> gotos, List<Action> actions) {
        this.stateNumber = stateNumber;
        this.gotos = gotos;
        this.actions = actions;
    }

    public List<ActionItem> getActionItems(int currentToken) {
        
        List <ActionItem> ret = new Vector<ActionItem>();
        
        for(Action a : actions) {
            a.accepts(currentToken);
            ret.addAll(a.getActionItems());
        }
        return ret;
    }

    public int go(int labelNumber) {
        // FIXME: I can't see how this is correct
        for(Goto g : gotos) {
            if(g.hasProd(labelNumber))
                return g.nextState;
        }
        return -1;
    }

    public boolean rejectable() {
        for(Action a : actions) {
            if(a.rejectable()) 
                return true;
        }
        return false;
    }

}
