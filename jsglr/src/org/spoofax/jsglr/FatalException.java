/*
 * Created on 06.des.2005
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr;

public class FatalException extends Exception {

 
    private static final long serialVersionUID = -7565203797064665307L;
    private String reason;
    
    public FatalException(String reason) {
        this.reason = reason;
    }

    public String toString() {
        return reason;
    }
}
