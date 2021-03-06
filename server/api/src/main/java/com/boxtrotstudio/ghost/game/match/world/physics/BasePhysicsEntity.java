package com.boxtrotstudio.ghost.game.match.world.physics;

import com.boxtrotstudio.ghost.game.match.entities.BaseEntity;
import com.boxtrotstudio.ghost.game.match.entities.Entity;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.entities.ability.BulletEntity;
import com.boxtrotstudio.ghost.game.match.world.World;
import com.boxtrotstudio.ghost.utils.PRunnable;
import com.boxtrotstudio.ghost.utils.Vector2f;
import com.boxtrotstudio.ghost.utils.VectorUtils;

public abstract class BasePhysicsEntity extends BaseEntity implements PhysicsEntity {
    private int id;
    protected PolygonHitbox hitbox;

    public BasePhysicsEntity() {
        setName("PHYSICS ENTITY");
        if (isStaticPhysicsObject()) {
            sendUpdates(false);
            requestTicks(false);
        }
    }

    protected void showHitbox() {
        for (Vector2f point : hitbox.getPolygon().getPoints()) {
            BulletEntity bPoint = new BulletEntity(null);
            bPoint.setPosition(point);
            bPoint.setVelocity(new Vector2f(0f, 0f));
            bPoint.requestTicks(false);
            getWorld().spawnEntity(bPoint);
        }
    }

    public abstract boolean isStaticPhysicsObject();

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    @Override
    public void setPosition(Vector2f vector2f) {
        if (hitbox != null) {
            Vector2f diff = new Vector2f(vector2f.x - position.x, vector2f.y - position.y);
            hitbox.getPolygon().translate(diff);
        }
        super.setPosition(vector2f);
    }

    @Override
    public void setRotation(double rotation) {
        if (hitbox != null) {
            double diff = rotation - super.rotation;
            hitbox.getPolygon().rotate(diff);
        }

        super.setRotation(rotation);
    }
    
    @Override
    public boolean intersects(PlayableEntity player) {
        return getHitbox().isHitboxInside(player.getHitbox()).didHit();
    }

    @Override
    public void setWorld(World world) {
        if (isStaticPhysicsObject()) {
            if (world == null) {
                super.getWorld().getPhysics().removePhysicsEntity(id);
            }
            super.setWorld(world);

            if (world != null) {
                if (hitbox == null) {
                    Vector2f[] points = generateHitboxPoints();

                    points = VectorUtils.rotatePoints(getRotation(), getPosition(), points);

                    hitbox = new PolygonHitbox(getName(), points);
                    hitbox.getPolygon().saveRotation(getRotation());
                }

                id = world.getPhysics().addPhysicsEntity(onHit, onComplexHit, hitbox);
            }
        } else {
            super.setWorld(world);
        }
    }

    public Vector2f findClosestIntersectionPoint(Vector2f startPos, Vector2f endPos) {
        double d = 0;
        Vector2f point = null;
        for (Face face : hitbox.getPolygon().getFaces()) {
            Vector2f intersect = VectorUtils.pointOfIntersection(startPos, endPos, face.getPointA(), face.getPointB());
            if (intersect == null)
                continue;

            double distance = Vector2f.distance(intersect, startPos);
            if (point == null || distance < d) {
                point = intersect;
                d = distance;
            }
        }

        return point;
    }

    public abstract Vector2f[] generateHitboxPoints();

    public abstract void onHit(Entity entity);

    public abstract void onHit(CollisionResult entity);

    private final PRunnable<Entity> onHit = this::onHit;
    private final PRunnable<CollisionResult> onComplexHit = this::onHit;
}
