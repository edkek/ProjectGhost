package com.boxtrotstudio.ghost.game.match.item;

import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.abilities.CancelableAbility;
import com.boxtrotstudio.ghost.game.match.entities.items.ItemEntity;
import com.boxtrotstudio.ghost.game.match.entities.items.SpeedItemEntity;
import com.boxtrotstudio.ghost.game.match.stats.BuffType;

/**
 * An item that speeds the player up for 10 seconds.
 *
 * Example item. Almost everything is documented.
 */
public class SpeedItem extends Item {

    public SpeedItem(LiveMatch match) {
        super(match);
    }

    @Override
    public long getDuration() {
        return 10_000;
    }

    @Override
    protected Class<? extends ItemEntity> getEntityClass() {
        return SpeedItemEntity.class;
    }

    @Override
    protected void onActivated() {
        //Add a new percent buff, and make it non stackable (new values replace old ones)
        activator.getSpeedStat().addBuff("speed_item_buff", BuffType.PercentAddition, 50, false);
        activator.onStatUpdate(activator.getSpeedStat());

        if (activator.currentAbility() instanceof CancelableAbility) {
            ((CancelableAbility)activator.currentAbility()).cancel();
        }
    }

    @Override
    protected void handleLogic() {
        if (System.currentTimeMillis() - activationTime >= 10_000) {
            //Remove the buff by name, and despawn the item completely
            //(This gets rid of the Item only, the Entity was despawned when it was collected
            activator.getSpeedStat().removeBuff("speed_item_buff");
            activator.onStatUpdate(activator.getSpeedStat());
            deactivate();
        }
    }
}
