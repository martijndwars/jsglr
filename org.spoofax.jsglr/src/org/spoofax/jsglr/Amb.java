/*
 * Created on 30.mar.2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr;

public class Amb implements IParseNode {

    public final IParseNode kid;
    
    Amb(IParseNode kid) {
        this.kid = kid; 
    }
    
    @Override
    public String toString() {
        return "amb(" + kid + ")";
    }
}