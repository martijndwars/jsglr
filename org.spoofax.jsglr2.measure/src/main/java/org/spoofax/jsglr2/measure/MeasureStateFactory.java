package org.spoofax.jsglr2.measure;

import java.util.ArrayList;
import java.util.List;

import org.spoofax.jsglr2.actions.ActionsPerCharacterClass;
import org.spoofax.jsglr2.actions.IGoto;
import org.spoofax.jsglr2.characterclasses.ICharacterClass;
import org.spoofax.jsglr2.states.IState;
import org.spoofax.jsglr2.states.StateFactory;

public class MeasureStateFactory extends StateFactory {

    public int statesCount = 0;
    public int statesDisjointSortableCharacterClassesCount = 0;

    public int gotosCount = 0;
    public int actionCharacterClassCount = 0;
    public int actionsCount = 0;

    public int gotosPerStateMax = 0;
    public int actionCharacterClassPerStateMax = 0;
    public int actionsPerStateMax = 0;
    public int actionsPerCharacterClassMax = 0;

    @Override public IState from(int stateNumber, IGoto[] gotos,
        ActionsPerCharacterClass[] actionsPerCharacterClasses) {
        statesCount++;

        gotosCount += gotos.length;
        actionCharacterClassCount += actionsPerCharacterClasses.length;

        int actionsCount = 0;

        List<ICharacterClass> characterClasses = new ArrayList<>(actionsPerCharacterClasses.length);

        for(ActionsPerCharacterClass actionsPerCharacterClass : actionsPerCharacterClasses) {
            actionsCount += actionsPerCharacterClass.actions.size();

            actionsPerCharacterClassMax =
                Math.max(actionsPerCharacterClassMax, actionsPerCharacterClass.actions.size());

            characterClasses.add(actionsPerCharacterClass.characterClass);
        }

        statesDisjointSortableCharacterClassesCount += ICharacterClass.disjointSortable(characterClasses) ? 1 : 0;

        this.actionsCount += actionsCount;

        gotosPerStateMax = Math.max(gotosPerStateMax, gotos.length);
        actionCharacterClassPerStateMax =
            Math.max(actionCharacterClassPerStateMax, actionsPerCharacterClasses.length);
        actionsPerStateMax = Math.max(actionsPerStateMax, actionsCount);

        return super.from(stateNumber, gotos, actionsPerCharacterClasses);
    }

}
