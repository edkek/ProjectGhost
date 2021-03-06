package com.boxtrotstudio.ghost.common.network.packet;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.network.BasePlayerClient;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.game.match.LiveMatch;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.world.timeline.EntitySnapshot;
import com.boxtrotstudio.ghost.game.match.world.timeline.WorldSnapshot;
import com.boxtrotstudio.ghost.network.packet.Packet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BulkEntityStatePacket extends Packet<BaseServer, BasePlayerClient> {
    public BulkEntityStatePacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        WorldSnapshot snapshot = (WorldSnapshot) args[0];
        LiveMatch match = (LiveMatch)args[1];
        //List<Entity> toUpdate = (List<Entity>) args[0];

        int lastWrite = client.getWriteNumber();

        List<EntitySnapshot> snapshots = calculateSendArray(client, snapshot.getEntitySnapshots(), match);

        write((byte)0x04);
        write(lastWrite);
        write(snapshots.size()); //Amount of entities in this bulk

        for (EntitySnapshot entity : snapshots) {
            writeEntity(client, entity, match);
        }

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }

    private List<EntitySnapshot> calculateSendArray(BasePlayerClient client, EntitySnapshot[] array, LiveMatch match) {
        if (client.getPlayer().isSpectating()) {
            ArrayList<EntitySnapshot> snapshots = new ArrayList<>();
            for (EntitySnapshot entity : array) {
                if (entity == null)
                    continue;
                snapshots.add(entity);
            }

            return snapshots;
        }

        ArrayList<EntitySnapshot> snapshots = new ArrayList<>();

        Player p = client.getPlayer();
        for (EntitySnapshot entity : array) {
            if (entity == null)
                continue;

            if (entity.isPlayer() && p.isInMatch() && !p.getTeam().isAlly(entity)) {
                PlayableEntity p1 = match.getWorld().getEntity(entity.getID());
                if (!p1.shouldSendUpdatesTo(p))
                    continue;
            }

            snapshots.add(entity);
        }

        return snapshots;
    }

    private void writeEntity(BasePlayerClient client, EntitySnapshot entity, LiveMatch match) throws IOException {
        if (entity == null)
            return;

        short id = client.getPlayer().getID() == entity.getID() ? 0 : entity.getID();
        int iAlpha = entity.getAlpha();
        if (id == 0) {
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean isPlayer = entity.isPlayer();
        boolean hasTarget = isPlayer && entity.hasTarget();
        if (id != 0) {
            if (client.getPlayer().isSpectating() || (client.getPlayer().getTeam().isAlly(entity) && entity.isVisibleToAllies())) { //Allies are always visible
                if (iAlpha < 150)
                    iAlpha = 150;
            }
        }

        //byte alpha = (byte)iAlpha; java bytes can suck my dick
        write(id)
                .write(entity.getX())
                .write(entity.getY())
                .write(entity.getVelX())
                .write(entity.getVelY())
                .write(iAlpha)
                .write(entity.getRotation())
                .write(match.getTimeElapsed())
                .write(hasTarget);

        if (hasTarget) {

            write(entity.getTargetX())
                    .write(entity.getTargetY());
        }

        /*if (isPlayer && id == 0) {
            BaseNetworkPlayer p = (BaseNetworkPlayer) entity;

            write(p.getVisibleIndicatorPosition());
        }*/
    }
}
