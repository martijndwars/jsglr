/*
 * Created on 30.mar.2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk near strategoxt.org>
 *
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr.client;

import org.spoofax.jsglr.client.imploder.TopdownTreeBuilder;


public abstract class AbstractParseNode {

    public static final int PARSE_PRODUCTION_NODE = 1;
    public static final int PARSENODE = 2;
    public static final int AMBIGUITY = 3;
    public static final int PREFER = 4;
    public static final int AVOID = 5;
    public static final int REJECT = 6;
    
    public boolean isAmbNode(){
    	return getNodeType()==AbstractParseNode.AMBIGUITY;
    }

    public boolean isParseNode(){
    	return (getNodeType()==AbstractParseNode.PARSENODE
    		|| getNodeType()==AbstractParseNode.REJECT
    		|| getNodeType()== AbstractParseNode.PREFER
    		|| getNodeType()==AbstractParseNode.AVOID
    	);
    }

    public boolean isParseRejectNode(){
    	return getNodeType()==AbstractParseNode.REJECT;
    }

    public boolean isParseProductionNode(){
    	return getNodeType()==AbstractParseNode.PARSE_PRODUCTION_NODE;
    }

    abstract public int getNodeType();
    abstract public AbstractParseNode[] getChildren();
    
    protected static final int NO_HASH_CODE = 0;

    public abstract Object toTreeBottomup(BottomupTreeBuilder builder);
    
    public abstract Object toTreeTopdown(TopdownTreeBuilder builder);
    
    @Override
	abstract public boolean equals(Object obj);
    
    @Override
	abstract public int hashCode();
    
    public void setCachedHashCode(){
    	//Hashcode is cached for ParseNodes (not for PPNs)
    }

    abstract public String toStringShallow();
    
    @Override
	abstract public String toString();
    
    /**
     * Returns true if this is either:
     * - a {@link ParseProductionNode}.
     * - a ParseNode with a {@link ParseProductionNode} child
     *   and an {@link #isParseProductionChain()} child.
     * - a ParseNode with a single {@link #isParseProductionChain()}
     *   child.
     */
    public abstract boolean isParseProductionChain();
}
