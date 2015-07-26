package me.eddiep.ghost.game.match.entities.items;


import me.eddiep.ghost.game.match.LiveMatch;

public class SpeedItemEntity extends ItemEntity {

    public SpeedItemEntity(LiveMatch match) {
        super(match);
    }

    @Override
    public byte getType() {
        return 10;
    }
}