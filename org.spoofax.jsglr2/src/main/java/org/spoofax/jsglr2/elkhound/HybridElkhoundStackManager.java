package org.spoofax.jsglr2.elkhound;

import org.spoofax.jsglr2.parseforest.AbstractParseForest;
import org.spoofax.jsglr2.parser.Position;
import org.spoofax.jsglr2.states.IState;

public class HybridElkhoundStackManager<ParseForest extends AbstractParseForest>
    extends AbstractElkhoundStackManager<ParseForest, AbstractElkhoundStackNode<ParseForest>> {

    @Override
    protected HybridElkhoundStackNode<ParseForest> createStackNode(int stackNumber, IState state, Position position,
        boolean isRoot) {
        return new HybridElkhoundStackNode<ParseForest>(stackNumber, state, position, isRoot);
    }

}