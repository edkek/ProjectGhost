package com.boxtrotstudio.ghost.game.match.abilities;

import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;

public interface Ability<T extends PlayableEntity> {

    /**
     * The name of this ability
     * @return The name as a String
     */
    String name();

    /**
     * The owner of this ability.
     * @return The owner
     */
    T owner();

    /**
     * The entity has executed this ability primary ability with the mouse position at <b>targetX</b> and <b>targetY</b>
     * @param targetX The X position this ability was used
     * @param targetY The Y position this ability was used
     */
    void usePrimary(float targetX, float targetY);

    /**
     * The entity has executed this ability secondary ability with the mouse position at <b>targetX</b> and <b>targetY</b>
     * @param targetX The X position this ability was used
     * @param targetY The Y position this ability was used
     */
    void useSecondary(float targetX, float targetY);

    byte id();

    boolean canFirePrimary();

    boolean canFireSecondary();
}
