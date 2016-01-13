package me.eddiep.ghost.game.match.entities;

import me.eddiep.ghost.game.match.Event;
import me.eddiep.ghost.game.match.LiveMatch;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.Vector2f;

public interface Entity {
    String getName();

    void setName(String name);

    LiveMatch getMatch();

    void setMatch(LiveMatch containingMatch);

    World getWorld();

    void setWorld(World world);

    boolean isInMatch();

    Entity getParent();

    void setParent(Entity parent);

    Vector2f getPosition();

    void setPosition(Vector2f position);

    Vector2f getVelocity();

    short getWidth();

    short getHeight();

    void setWidth(short width);

    void setHeight(short height);

    boolean isDefaultSize();

    void setVelocity(Vector2f velocity);

    float getX();

    float getY();

    double getRotation();

    void setRotation(double rotation);

    float getXVelocity();

    float getYVelocity();

    void setVelocity(float xvel, float yvel);

    void tick();

    void setID(short ID);

    short getID();

    boolean isInside(float xmin, float ymin, float xmax, float ymax);

    int getAlpha();

    void setAlpha(int alpha);

    void setAlpha(float alpha);

    boolean isVisible();

    /**
     * Whether this entity bounces when it collides with a mirror. If this entity does not bounce then it will
     * act as though it hit a wall when collided with a mirror
     * @return True if this entity should bounce off the mirror, otherwise false
     */
    boolean doesBounce();

    void setVisible(boolean visible);

    /**
     * Fades out the entity without despawning it. Equivalent to {@code fadeOut(false, duration); }
     *
     * @see PlayableEntity#fadeOut(boolean, long)
     */
    void fadeOut(long duration);

    /**
     * Fades out the entity without despawning it. Equivalent to {@code fadeOut(true, duration); }
     *
     * @see PlayableEntity#fadeOut(boolean, long)
     */
    void fadeOutAndDespawn(long duration);

    /**
     * Fades out this entity after a certain duration and optionally despawns it. If the entity is not already
     * visible, the fade out will have no effect.
     *
     * @param duration The amount of time until the entity is completely invisible.
     * @param despawn Whether to despawn the entity after the fade-out.
     */
    void fadeOut(final boolean despawn, final long duration);

    void shake(long duration);

    void shake(final long duration, final double shakeWidth, final double shakeIntensity);

    boolean isSendingUpdates();

    void sendUpdates(boolean update);

    boolean isRequestingTicks();

    void requestTicks(boolean request);

    void onCollision(PhysicsEntity contacter);

    /**
     * Determines whether this entity intersects with a <b>player</b>
     * @param player The player to check
     * @return True if this entity and the <b>player</b> are intersecting. Otherwise false
     */
    boolean intersects(PlayableEntity player);

    void despawn();

    void triggerEvent(Event event);
}
