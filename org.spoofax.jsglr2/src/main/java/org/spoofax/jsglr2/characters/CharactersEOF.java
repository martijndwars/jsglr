package org.spoofax.jsglr2.characters;

public final class CharactersEOF implements ICharacters {

    public static final CharactersEOF INSTANCE = new CharactersEOF();

    public boolean containsCharacter(int character) {
        return false;
    }

    public final boolean containsEOF() {
        return true;
    }

    public final <C extends Number & Comparable<C>> CharacterClassRangeSet<C>
        rangeSetUnion(CharacterClassRangeSet<C> rangeSet) {
        return rangeSet.addEOF();
    }

    @Override public final String toString() {
        return "{EOF}";
    }

}
