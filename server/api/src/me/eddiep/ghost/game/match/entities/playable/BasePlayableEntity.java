package me.eddiep.ghost.game.match.entities.playable;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.match.abilities.Ability;
import me.eddiep.ghost.game.match.abilities.Gun;
import me.eddiep.ghost.game.match.world.timeline.EntitySnapshot;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.game.util.VisibleFunction;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.TimeUtils;
import me.eddiep.ghost.utils.Vector2f;

import java.lang.reflect.InvocationTargetException;
import java.security.InvalidParameterException;

import static me.eddiep.ghost.utils.Constants.*;

public abstract class BasePlayableEntity extends BaseEntity implements PlayableEntity {
    private static final byte MAX_LIVES = 3;
    private static final float VISIBLE_TIMER = 800f;

    protected byte lives;
    protected boolean isDead;
    protected boolean frozen;
    protected boolean isReady;
    protected float speed = 6f;
    protected long lastFire;
    protected boolean wasHit;
    protected long lastHit;
    protected boolean didFire = false;
    protected Vector2f target;

    protected boolean canFire = true;
    protected VisibleFunction function = VisibleFunction.ORGINAL; //Always default to original style
    private Ability<PlayableEntity> ability = new Gun(this);

    @Override
    public Team getTeam() {
        return containingMatch == null ? null : containingMatch.getTeamFor(this);
    }

    @Override
    public void prepareForMatch() {
        oldVisibleState = true;
        setVisible(false);
    }

    @Override
    public void onFire() {
        lastFire = System.currentTimeMillis();
        didFire = true;
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }
    }

    protected void handleVisible() {
        switch (function) {
            case ORGINAL:
                if (didFire) {
                    if (isVisible() && System.currentTimeMillis() - lastFire >= VISIBLE_TIMER) {
                        didFire = false;
                        startPlayerFadeOut();
                    }
                } else if (wasHit) {
                    if (isVisible() && System.currentTimeMillis() - lastHit >= VISIBLE_TIMER) {
                        wasHit = false;
                        startPlayerFadeOut();
                    }
                }
                break;

            case TIMER:
                if (getMatch().hasMatchStarted()) {
                    handleVisibleState();
                }
                break;

        }
    }

    private boolean hasStartedFade = false;
    private long startTime;
    private void fadePlayerOut() {
        if (!hasStartedFade)
            return;

        if (didFire || wasHit) {
            alpha = 255;
            hasStartedFade = false;
            return;
        }

        alpha = (int) TimeUtils.ease(255f, 0f, FADE_SPEED, System.currentTimeMillis() - startTime);

        if (alpha == 0) {
            hasStartedFade = false;
        }
    }

    private void startPlayerFadeOut() {
        if (hasStartedFade)
            return;

        hasStartedFade = true;
        startTime = System.currentTimeMillis();
    }


    public int getVisibleIndicatorPosition() {
        return visibleIndicator;
    }


    int visibleIndicator;
    private void handleVisibleState() {
        if (didFire || wasHit) {
            visibleIndicator -= VISIBLE_COUNTER_DECREASE_RATE;
            if (visibleIndicator <= 0) {
                visibleIndicator = 0;
                alpha = 0;
                didFire = false;
                wasHit = false;
            }
        } else {
            visibleIndicator += VISIBLE_COUNTER_INCREASE_RATE;
        }

        if (visibleIndicator < VISIBLE_COUNTER_START_FADE) {
            alpha = 0;
        } else if (visibleIndicator > VISIBLE_COUNTER_START_FADE && visibleIndicator < VISIBLE_COUNTER_FULLY_VISIBLE) {
            int totalDistance = VISIBLE_COUNTER_FADE_DISTANCE;
            int curDistance = visibleIndicator - VISIBLE_COUNTER_START_FADE;

            alpha = Math.max(Math.min((int) (((double)curDistance / (double)totalDistance) * 255.0), 255), 0);

        } else if (visibleIndicator > VISIBLE_COUNTER_FULLY_VISIBLE) {
            alpha = 255;
        }
    }

    @Override
    public void onDamage(PlayableEntity damager) {
        wasHit = true;

        lastHit = System.currentTimeMillis();
        switch (function) {
            case ORGINAL:
                if (!isVisible())
                    setVisible(true);
                break;
            case TIMER:
                if (visibleIndicator < VISIBLE_COUNTER_DEFAULT_LENGTH) {
                    visibleIndicator = VISIBLE_COUNTER_DEFAULT_LENGTH;
                }
                break;
        }
    }

    @Override
    public void tick() {
        if (hasTarget()) {
            if (Math.abs(position.x - target.x) < 8 && Math.abs(position.y - target.y) < 8) {
                setPosition(target);
                target = null;
                setVelocity(new Vector2f(0f, 0f));
                world.requestEntityUpdate();
            }
        }

        position.x += velocity.x;
        position.y += velocity.y;

        handleVisible();

        fadePlayerOut();
    }

    @Override
    public Vector2f getTarget() {
        return target;
    }

    @Override
    public boolean hasTarget() { return target != null; }

    @Override
    public void subtractLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives--;
        if (lives <= 0) {
            isDead = true;
            frozen = true;
            setVisible(true);
        }
        getMatch().playableUpdated(this);
    }

    @Override
    public void addLife() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives++;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        getMatch().playableUpdated(this);
    }

    @Override
    public void resetLives() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = MAX_LIVES;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        getMatch().playableUpdated(this);
    }

    @Override
    public void setVisibleFunction(VisibleFunction function) {
        this.function = function;
    }

    @Override
    public VisibleFunction getVisibleFunction() {
        return function;
    }

    @Override
    public void setLives(byte value) {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");
        if (value <= 0)
            throw new InvalidParameterException("Invalid argument!\nTo set the playable's lives to 0, please use the kill() function!");

        lives = value;
        if (isDead) {
            isDead = false;
            frozen = false;
            setVisible(false);
        }
        getMatch().playableUpdated(this);
    }

    @Override
    public void kill() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        lives = 0;
        isDead = true;
        frozen = true;
        setVisible(true);
        getMatch().playableUpdated(this);
    }

    @Override
    public void freeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = true;
        getMatch().playableUpdated(this);
    }

    @Override
    public void unfreeze() {
        if (!isInMatch())
            throw new IllegalStateException("This playable is not in a match!");

        frozen = false;
        getMatch().playableUpdated(this);
    }

    @Override
    public boolean shouldSendUpdatesTo(PlayableEntity e) {
        if (ArrayHelper.contains(getOpponents(), e)) { //e is an opponent
            if (alpha > 0 || (alpha == 0 && oldVisibleState)) {
                oldVisibleState = alpha != 0;

                return true;
            } else {
                return false;
            }
        } else return true;

    }

    @Override
    public byte getLives() {
        return lives;
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public boolean isFrozen() {
        return frozen;
    }

    @Override
    public boolean isReady() {
        return isReady;
    }

    @Override
    public void setReady(boolean ready) {
        this.isReady = ready;
    }

    @Override
    public PlayableEntity[] getOpponents() {
        if (!isInMatch())
            return new PlayableEntity[0];

        if (getMatch().getTeam1().isAlly(this))
            return getMatch().getTeam2().getTeamMembers();
        else if (getMatch().getTeam2().isAlly(this))
            return getMatch().getTeam1().getTeamMembers();
        else
            return new PlayableEntity[0];
    }

    @Override
    public PlayableEntity[] getAllies() {
        if (getTeam() == null)
            return new PlayableEntity[0];

        return getTeam().getTeamMembers();
    }

    @Override
    public Ability<PlayableEntity> currentAbility() {
        return ability;
    }

    @Override
    public void setCurrentAbility(Class<? extends Ability<PlayableEntity>> class_) {
        try {
            this.ability = class_.getConstructor(PlayableEntity.class).newInstance(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("This ability is not compatible!");
        }
    }

    public void useAbility(float targetX, float targetY, int action) {
        if (!canFire)
            return; //This playable can't use abilities

        if (ability != null) {
            ability.use(targetX, targetY, action);

            if (isVisible()) {
                hasStartedFade = false;
                alpha = 255;
            }
        }
    }

    @Override
    public boolean canFire() {
        return canFire;
    }

    @Override
    public void setCanFire(boolean val) {
        this.canFire = val;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }
}