package me.eddiep.ghost.test.game.queue.impl;

import me.eddiep.ghost.game.entities.PlayableEntity;
import me.eddiep.ghost.game.entities.abilities.Circle;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.util.VisibleFunction;

public class CircleQueue extends DemoQueue {
    @Override
    public void setupPlayer(PlayableEntity p) {
        p.setLives((byte) 3);
        p.setVisibleFunction(VisibleFunction.ORGINAL);
        p.setCurrentAbility(Circle.class);
    }

    @Override
    public String description() {
        return "Fire dem lasers";
    }

    @Override
    public Queues queue() {
        return Queues.LASER;
    }

    @Override
    public int allyCount() {
        return 0;
    }

    @Override
    public int opponentCount() {
        return 1;
    }
}