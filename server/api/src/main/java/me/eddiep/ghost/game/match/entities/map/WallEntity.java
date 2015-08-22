package me.eddiep.ghost.game.match.entities.map;

import me.eddiep.ghost.game.match.entities.BaseEntity;
import me.eddiep.ghost.game.match.entities.Entity;
import me.eddiep.ghost.game.match.world.World;
import me.eddiep.ghost.game.match.world.physics.Hitbox;
import me.eddiep.ghost.game.match.world.physics.PhysicsEntity;
import me.eddiep.ghost.utils.PRunnable;
import me.eddiep.ghost.utils.Vector2f;
import me.eddiep.ghost.utils.VectorUtils;

public class WallEntity extends BaseEntity implements PhysicsEntity {
    private static final float WIDTH = 250f;
    private static final float HEIGHT = 128f;

    private Hitbox hitbox;
    private int id;
    public WallEntity() {
        sendUpdates(false); //Walls don't need updates
        requestTicks(false); //Walls don't need ticks
        setName("WALL");
    }

    @Override
    public void setWorld(World world) {
        if (world == null) {
            super.getWorld().getPhysics().removePhysicsEntity(id);
        }
        super.setWorld(world);

        if (world != null) {
            if (hitbox == null) {
                float x1 = getX() - (WIDTH / 2f), x2 = getX() + (WIDTH / 2f);
                float y1 = getY() - (HEIGHT / 2f), y2 = getY() + (HEIGHT / 2f);

                Vector2f[] points = new Vector2f[] {
                        new Vector2f(x1, y1),
                        new Vector2f(x1, y2),
                        new Vector2f(x2, y2),
                        new Vector2f(x2, y1)
                };

                points = VectorUtils.rotatePoints(getRotation(), getPosition(), points);

                hitbox = new Hitbox(points);
            }

            id = world.getPhysics().addPhysicsEntity(onHit, hitbox);
        }
    }

    @Override
    public void tick() { }

    @Override
    public byte getType() {
        return 80; //Items should start at -127
    }

    @Override
    public Hitbox getHitbox() {
        return hitbox;
    }

    private final PRunnable<Entity> onHit = new PRunnable<Entity>() {
        @Override
        public void run(Entity p) {
            p.getWorld().despawnEntity(p);
        }
    };
}
