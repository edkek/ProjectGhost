package me.eddiep.ghost.client.core;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import me.eddiep.ghost.client.Ghost;
import me.eddiep.ghost.client.utils.Vector2f;
import me.eddiep.ghost.client.utils.annotations.InternalOnly;

import java.util.ArrayList;

public class Entity extends Sprite implements Drawable, Logical, Attachable {
    private float z;
    private boolean hasLoaded = false;
    private short id;

    private Vector2f velocity = new Vector2f(0f, 0f);
    private Vector2f target;

    private Vector2f inter_target, inter_start;
    private long inter_duration, inter_timeStart;
    private boolean interpolate = false;
    private Blend blend;

    private ArrayList<Attachable> children = new ArrayList<Attachable>();
    private ArrayList<Attachable> parents = new ArrayList<Attachable>();

    private final Object child_lock = new Object();

    public static Entity fromImage(String path) {
        Texture texture = Ghost.ASSETS.get(path, Texture.class);
        Sprite sprite = new Sprite(texture);
        return new Entity(sprite, (short)0);
    }

    public static Entity fromImage(String path, short id) {
        Texture texture = Ghost.ASSETS.get(path, Texture.class);
        Sprite sprite = new Sprite(texture);
        return new Entity(sprite, id);
    }

    public Entity(Sprite sprite, short id) {
        super(sprite);

        setOriginCenter();

        this.id = id;
    }

    protected Entity(String path, short id) {
        super(Ghost.ASSETS.get(path, Texture.class));

        this.id = id;
    }

    @Override
    public Blend blendMode() {
        return blend;
    }

    public void setBlend(Blend blend) {
        this.blend = blend;
    }

    public short getID() {
        return id;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public void setTarget(Vector2f target) {
        this.target = target;
    }

    public Vector2f getTarget() {
        return target;
    }

    public float getCenterX() {
        return getX() + (getWidth() / 2f);
    }

    public float getCenterY() {
        return getY() + (getWidth() / 2f);
    }

    @InternalOnly
    public final void load() {
        onLoad();
        if (!hasLoaded)
            throw new IllegalStateException("super.onLoad() was not invoked!");
    }

    protected void onLoad() {
        hasLoaded = true;
    }

    @InternalOnly
    public final void unload() {
        onUnload();
        if (hasLoaded)
            throw new IllegalStateException("super.onUnload() was not invoked!");
    }

    protected void onUnload() {
        hasLoaded = false;
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);
    }

    @Override
    public void setX(float x) {
        float dif = getX() - x;
        super.setX(x);

        synchronized (child_lock) {
            for (Attachable c : children) {
                c.setX(c.getX() - dif);
            }
        }
    }

    @Override
    public void setY(float y) {
        float dif = getY() - y;
        super.setY(y);

        synchronized (child_lock) {
            for (Attachable c : children) {
                c.setY(c.getY() - dif);
            }
        }
    }

    public void attach(Attachable e) {
        synchronized (child_lock) {
            children.add(e);
        }
        e.addParent(this);
    }

    public void deattach(Attachable e) {
        synchronized (child_lock) {
            children.remove(e);
        }
        e.removeParent(this);
    }

    @Override
    public void addParent(Attachable e) {
        parents.add(e);
    }

    @Override
    public void removeParent(Attachable e) {
        parents.remove(e);
    }

    @Override
    public void tick() {
        if (!interpolate) {
            if (target != null) {
                if (Math.abs(getX() - target.x) < 8 && Math.abs(getY() - target.y) < 8) {
                    velocity.x = velocity.y = 0;
                }
            }

            setX(getX() + velocity.x);
            setY(getY() + velocity.y);
        } else {
            float x = ease(inter_start.x, inter_target.x, System.currentTimeMillis() - inter_timeStart, inter_duration);
            float y = ease(inter_start.y, inter_target.y, System.currentTimeMillis() - inter_timeStart, inter_duration);

            setX(x);
            setY(y);

            if (x == inter_target.x && y == inter_target.y) {
                interpolate = false;
            }
        }
    }

    @Override
    public void dispose() { }

    public void interpolateTo(float x, float y, long duration) {
        inter_start = new Vector2f(getX(), getY());
        inter_target = new Vector2f(x, y);
        inter_timeStart = System.currentTimeMillis();
        inter_duration = duration;
        interpolate = true;
    }

    //Code taken from: https://code.google.com/p/replicaisland/source/browse/trunk/src/com/replica/replicaisland/Lerp.java?r=5
    //Because I'm a no good dirty scrub
    public static float ease(float start, float target, float duration, float timeSinceStart) {
        float value = start;
        if (timeSinceStart > 0.0f && timeSinceStart < duration) {
            final float range = target - start;
            final float percent = timeSinceStart / (duration / 2.0f);
            if (percent < 1.0f) {
                value = start + ((range / 2.0f) * percent * percent * percent);
            } else {
                final float shiftedPercent = percent - 2.0f;
                value = start + ((range / 2.0f) *
                        ((shiftedPercent * shiftedPercent * shiftedPercent) + 2.0f));
            }
        } else if (timeSinceStart >= duration) {
            value = target;
        }
        return value;
    }

    public Vector2f getPosition() {
        return new Vector2f(getCenterX(), getCenterY());
    }
}
