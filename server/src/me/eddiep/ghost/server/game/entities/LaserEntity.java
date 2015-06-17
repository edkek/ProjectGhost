package me.eddiep.ghost.server.game.entities;

import me.eddiep.ghost.server.game.Entity;
import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.utils.MathUtils;

import java.io.IOException;
import java.util.ArrayList;

public class LaserEntity extends Entity implements TypeableEntity {
    private Playable parent;
    private ArrayList<Playable> alreadyHit = new ArrayList<>();
    public LaserEntity(Playable parent) {
        super();
        setParent(parent.getEntity());
        setMatch(parent.getMatch());
        setVisible(true);
        setName("LAZERS");
        this.parent = parent;
    }

    @Override
    public void tick() {
        super.tick();

        if (check) {
            //float currentWidth = TimeUtils.ease(0f, 1040f, 300f, System.currentTimeMillis() - start);

            float x = getX(), y = getY() + 32f;
            float bx = parent.getEntity().getX() + 1040;
            float by = parent.getEntity().getY() - 32f;

                                                               //Center of rotation
            Vector2f[] rect = MathUtils.rotatePoints(rotation, getPosition(),
                    new Vector2f(x, y),
                    new Vector2f(bx, y),
                    new Vector2f(bx, by),
                    new Vector2f(x, by)
            );

            Playable[] opponents = parent.getOpponents();
            for (Playable p : opponents) {
                if (alreadyHit.contains(p))
                    continue;

                Entity toHit = p.getEntity();
                if (MathUtils.isPointInside(toHit.getPosition(), rect)) {
                    p.subtractLife();
                    if (!toHit.isVisible()) {
                        toHit.setVisible(true);
                    }

                    p.onDamage(parent); //p was damaged by the parent

                    parent.onDamagePlayable(p); //the parent damaged p
                    if (p.isDead()) {
                        parent.onKilledPlayable(p);
                    }

                    alreadyHit.add(p);
                }
            }
        }
    }

    @Override
    public void updateState() throws IOException {
        Playable[] temp = parent.getOpponents();
        for (Playable p : temp) {
            p.updateEntity(this);
        }

        temp = parent.getAllies();
        for (Playable p : temp) {
            p.updateEntity(this);
        }
    }

    @Override
    public byte getType() {
        return 3;
    }

    private boolean check;
    private long start;
    public void startChecking() {
        check = true;
        start = System.currentTimeMillis();
    }
}
