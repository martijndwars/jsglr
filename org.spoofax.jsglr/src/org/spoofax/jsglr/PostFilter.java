/*
 * Created on 11.apr.2006
 *
 * Copyright (c) 2005, Karl Trygve Kalleberg <karltk@ii.uib.no>
 * 
 * Licensed under the GNU General Public License, v2
 */
package org.spoofax.jsglr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import aterm.ATerm;

public class PostFilter {

    SGLR parser;

    AmbiguityManager ambiguityManager;

    ParseTable parseTable;

    Map<Object, Integer> posTable;

    Map<Object, IParseNode> resolvedTable;

    private AmbiguityMap inputAmbiguityMap;

    private AmbiguityMap markMap;

    private PositionMap positionMap;

    private int tokenPosition;

    PostFilter(SGLR parser) {
        this.parser = parser;
    }

    private void initializeFromParser() {
        parseTable = parser.getParseTable();
        ambiguityManager = parser.getAmbiguityManager();
    }

    protected ATerm parseResult(IParseNode root, String sort, int inputLength) {

        initializeAmbiguityMaps(inputLength);
        initializeFromParser();

        IParseNode t = root;
        AmbiguityManager ambMgr = parser.getAmbiguityManager();
        ParseTable parseTable = parser.getParseTable();

        Tools.debug("pre-select: ", t);

        if (sort != null) {
            t = selectOnTopSort();
            if (t == null) {
                return parseError("Desired top sort not found");
            }
        }

        Tools.debug("pre-cycle detect: ", t);

        if (parser.isDetectCyclesEnabled()) {
            if (ambMgr.getMaxNumberOfAmbiguities() > 0) {
                if (isCyclicTerm(t))
                    parseError("Term is cyclic");
            }
        }

        Tools.debug("pre-filtering detect: ", t);

        if (parser.isFilteringEnabled()) {
            t = filterTree(t);
        }

        Tools.debug("pre-yield: ", t);

        if (t != null) {
            ATerm r = yieldTree(t);
            int ambCount = ambMgr.getAmbiguitiesCount();
            Tools.debug("yield: ", r);
            return parseTable.getFactory().parse("parsetree(" + r + "," + ambCount + ")");
        }

        return null;
    }

    private void initializeAmbiguityMaps(int inputLength) {
        inputAmbiguityMap = new AmbiguityMap(inputLength);
    }

    private ATerm yieldTree(IParseNode t) {
        return t.toParseTree(parser.getParseTable());
    }

    private IParseNode filterTree(IParseNode t) {
        ambiguityManager.resetClustersVisitedCount();
        return filterTree(t, 0, false);
    }

    private IParseNode filterTree(IParseNode t, int pos, boolean inAmbiguityCluster) {

        // if(SGLR.isDebugging()) {
        Tools.debug("filterTree() - " + t.getClass());
        // }

        List<IParseNode> ambs = null;
        Object key;
        IParseNode newT;

        // If APPL
        if (inputAmbiguityMapIsSet(pos)) {
            ambs = getCluster(t, pos);
        } else {
            ambs = getEmptyList();
        }

        if (!inAmbiguityCluster && !ambs.isEmpty()) {
            key = createAmbiguityKey(ambs, pos);

            newT = resolvedTable.get(key);
            if (newT == null) {
                newT = filterAmbiguities(ambs, pos);
                if (newT != null) {
                    resolvedTable.put(key, newT);
                    posTable.put(key, pos);
                } else {
                    return null;
                }
            } else {
                // FIXME
                pos = posTable.get(key);
            }
            t = newT;
        } else if (t instanceof ParseNode) {
            ParseNode node = (ParseNode) t;
            List<IParseNode> args = node.getKids();
            List<IParseNode> newArgs = filterTree(args, pos, false);

            if (parser.isFilteringEnabled()) {
                if (parser.isRejectFilterEnabled() && parseTable.hasRejects()) {
                    if (hasRejectProd(t))
                        return null;
                }
            }

            if (newArgs != null) {
                t = new ParseNode(node.label, newArgs);
            } else {
                return null;
            }
        } else {
            return t;
        }

        if (parser.isFilteringEnabled()) {
            return applyAssociativityPriorityFilter(t);
        } else {
            return t;
        }
    }

    private List<IParseNode> tail(List<IParseNode> list) {
        // FIXME This is ugly and slow!
        List<IParseNode> tail = new LinkedList<IParseNode>();
        tail.addAll(list);
        tail.remove(0);
        return tail;
    }

    private List<IParseNode> filterTree(List<IParseNode> args, int pos, boolean inAmbiguityCluster) {

        List<IParseNode> t = args;

        if (!args.isEmpty()) {
            IParseNode arg = args.get(0);

            List<IParseNode> tail = tail(args);
            IParseNode newArg = filterTree(arg, pos, false);

            List<IParseNode> newTail;

            if (tail.isEmpty()) {
                newTail = getEmptyList();
            } else {
                newTail = filterTree(tail, pos, false);

                if (newTail == null)
                    return null;
            }

            if (newArg != null) {
                if (!arg.equals(newArg) || !tail.equals(newTail)) {
                    newTail.add(0, newArg);
                    t = newTail;
                }
            } else {
                return null;
            }
        }

        if (parser.isFilteringEnabled()) {
            List<IParseNode> filtered = new ArrayList<IParseNode>();
            for (IParseNode n : t)
                filtered.add(applyAssociativityPriorityFilter(n));
            return filtered;
        } else {
            return t;
        }
    }

    private IParseNode applyAssociativityPriorityFilter(IParseNode t) {

        IParseNode r = t;

        if (t instanceof Amb) {
            Label prodLabel = getProductionLabel(t);

            if (parser.isAssociativityFilterEnabled()) {
                if (prodLabel.isLeftAssociative()) {
                    r = applyLeftAssociativeFilter(t, prodLabel);
                } else if (prodLabel.isRightAssociative()) {
                    throw new NotImplementedException();
                    // r = applyRightAssociativeFilter(t, prodLabel);
                }
            }

            if (r != null && parser.isPriorityFilterEnabled()) {
                if (lookupGtrPriority(prodLabel) != null) {
                    return applyPriorityFilter(r, prodLabel);
                }
            }
        }

        return r;
    }

    private IParseNode applyPriorityFilter(IParseNode r, Label prodLabel) {

        List<IParseNode> newAmbiguities = new ArrayList<IParseNode>();
        List<IParseNode> alternatives = ((Amb) r).getAlternatives();
        int l0 = prodLabel.labelNumber;

        for (IParseNode alt : alternatives) {
            IParseNode injection = jumpOverInjections(alt);

            if (injection instanceof Amb) {

            } else {
                int l1 = ((ParseNode) injection).label;
                if (hasGreaterPriority(l0, l1)) {
                    return null;
                }
            }
        }

        return null;
    }

    private IParseNode jumpOverInjections(IParseNode alt) {

        if (!(alt instanceof Amb)) {
            int prod = ((ParseNode) alt).label;
            while (isUserDefinedLabel(prod)) {
                List<IParseNode> kids = ((ParseNode) alt).getKids();
                throw new NotImplementedException();
            }
        } else {
            return alt;
        }

        return null;
    }

    private boolean isUserDefinedLabel(int prod) {
        return parseTable.lookupInjection(prod) != null;
    }

    /**
     * Returns true if the first production has higher priority than the second.
     * 
     * @param l0
     * @param l1
     * @return true if production l0 has greater priority than l1
     */
    private boolean hasGreaterPriority(int l0, int l1) {
        throw new NotImplementedException();
    }

    private List lookupGtrPriority(Label prodLabel) {
        return parseTable.getPriorities(prodLabel);
    }

    private IParseNode applyLeftAssociativeFilter(IParseNode t, Label prodLabel) {

        List<IParseNode> newAmbiguities = new ArrayList<IParseNode>();
        List<IParseNode> alternatives = ((Amb) t).getAlternatives();
        IParseNode last = alternatives.get(alternatives.size() - 1);

        if (last instanceof Amb) {
            List<IParseNode> rest = new ArrayList<IParseNode>();
            rest.addAll(alternatives);
            rest.remove(rest.size() - 1);

            List<IParseNode> ambs = ((Amb) last).getAlternatives();
            for (IParseNode amb : ambs) {
                Label other = parseTable.getLabel(((ParseNode) amb).getLabel());
                if (!prodLabel.equals(other)) {
                    newAmbiguities.add(amb);
                }
            }

            if (!newAmbiguities.isEmpty()) {
                if (newAmbiguities.size() > 1) {
                    last = new Amb(newAmbiguities);
                } else {
                    last = newAmbiguities.get(0);
                }
                rest.add(last);
                return new Amb(rest);
            } else {
                return null;
            }
        } else if (last instanceof ParseNode) {
            Label other = parseTable.getLabel(((ParseNode) last).getLabel());
            if (prodLabel.equals(other)) {
                return null;
            }
        }
        return null;
    }

    private Label getProductionLabel(IParseNode t) {
        if (t instanceof ParseNode) {
            return parseTable.getLabel(((ParseNode) t).getLabel());
        } else if (t instanceof ParseProductionNode) {
            return parseTable.getLabel(((ParseProductionNode) t).getProduction());
        }
        return null;
    }

    private boolean hasRejectProd(IParseNode t) {
        return t instanceof ParseReject;
    }

    private IParseNode filterAmbiguities(List<IParseNode> ambs, int pos) {
        throw new NotImplementedException();
    }

    private Object createAmbiguityKey(List<IParseNode> ambs, int pos) {
        throw new NotImplementedException();
    }

    private List<IParseNode> getEmptyList() {
        return new ArrayList<IParseNode>(0);
    }

    private List<IParseNode> getCluster(IParseNode t, int pos) {
        throw new NotImplementedException();
    }

    private boolean inputAmbiguityMapIsSet(int pos) {
        return inputAmbiguityMap.isMarked(pos);
    }

    private boolean isCyclicTerm(IParseNode t) {

        List<IParseNode> cycles = computeCyclicTerm(t);

        return cycles != null && cycles.size() > 0;
    }

    private List<IParseNode> computeCyclicTerm(IParseNode t) {
        PositionMap visited = new PositionMap(ambiguityManager.getMaxNumberOfAmbiguities());

        ambiguityManager.resetAmbiguityCount();
        initializeMarks();
        tokenPosition = 0;

        return computeCyclicTerm(t, false, visited);
    }

    private List<IParseNode> computeCyclicTerm(IParseNode t, boolean inAmbiguityCluster,
            PositionMap visited) {

        if (t instanceof ParseProductionNode) {
            tokenPosition++;
            return null;
        } else if (t instanceof ParseNode) {
            Amb ambiguities = null;
            List<IParseNode> cycle = null;
            int clusterIndex;
            ParseNode n = (ParseNode) t;

            if (inAmbiguityCluster) {
                cycle = computeCyclicTerm(n.getKids(), false, visited);
            } else {
                if (inputAmbiguityMap.isMarked(tokenPosition)) {
                    ambiguityManager.increaseAmbiguityCount();
                    clusterIndex = ambiguityManager.getClusterIndex(t, tokenPosition);
                    if (markMap.isMarked(clusterIndex)) {
                        return new ArrayList<IParseNode>();
                    }
                    ambiguities = ambiguityManager.getClusterOnIndex(clusterIndex);
                } else {
                    clusterIndex = -1;
                }

                if (ambiguities == null) {
                    cycle = computeCyclicTerm(((ParseNode) t).getKids(), false, visited);
                } else {
                    int length = visited.getValue(clusterIndex);
                    int savePos = tokenPosition;

                    if (length == -1) {
                        markMap.mark(clusterIndex);
                        cycle = computeCyclicTermInAmbiguityCluster(ambiguities, visited);
                        visited.put(clusterIndex, tokenPosition - savePos);
                        markMap.unmark(clusterIndex);
                    } else {
                        tokenPosition += length;
                    }
                }
            }
            return cycle;
        } else {
            throw new FatalError();
        }
    }

    private List<IParseNode> computeCyclicTermInAmbiguityCluster(Amb ambiguities,
            PositionMap visited) {
        throw new NotImplementedException();
    }

    private List<IParseNode> computeCyclicTerm(List<IParseNode> kids, boolean b, PositionMap visited) {

        for (IParseNode kid : kids) {
            List<IParseNode> cycle = computeCyclicTerm(kid, false, visited);
            if (cycle != null)
                return cycle;
        }
        return null;
    }

    private void initializeMarks() {
        markMap = new AmbiguityMap(1024);
    }

    private ATerm parseError(String msg) {
        System.err.println("Parse error: " + msg);
        return null;
    }

    private IParseNode selectOnTopSort() {
        throw new NotImplementedException();
    }

}