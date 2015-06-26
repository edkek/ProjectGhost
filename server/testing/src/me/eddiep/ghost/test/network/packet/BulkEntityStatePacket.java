package me.eddiep.ghost.test.network.packet;

import me.eddiep.ghost.game.Entity;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.test.network.TcpUdpClient;
import me.eddiep.ghost.test.network.TcpUdpServer;
import me.eddiep.ghost.test.game.Player;

import java.io.IOException;
import java.util.List;

public class BulkEntityStatePacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public BulkEntityStatePacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        List<Entity> toUpdate = (List<Entity>) args[0];

        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        write((byte)0x04);
        write(lastWrite);
        write(toUpdate.size()); //Amount of entities in this bulk

        for (Entity entity : toUpdate) {
            writeEntity(client, entity);
        }

        client.getServer().sendUdpPacket(
                endUDP()
        );
    }

    private void writeEntity(TcpUdpClient client, Entity entity) throws IOException {
        short id = client.getPlayer().equals(entity) ? 0 : entity.getID();
        int iAlpha = entity.getAlpha();
        if (entity.equals(client.getPlayer())) {
            if (iAlpha < 150)
                iAlpha = 150;
        }

        //boolean isVisible = entity.equals(client.getPlayer()) || entity.isVisible();
        boolean isPlayer = entity instanceof Player;
        boolean hasTarget = isPlayer && ((Player)entity).getTarget() != null;

        if (entity instanceof Player) {
            if (client.getPlayer().getTeam().isAlly((Player)entity)) { //Allies are always visible
                if (iAlpha < 150)
                    iAlpha = 150;
            }
        }

        //byte alpha = (byte)iAlpha; java bytes can suck my dick
         write(id)
                .write(entity.getPosition().x)
                .write(entity.getPosition().y)
                .write(entity.getVelocity().x)
                .write(entity.getVelocity().y)
                .write(iAlpha)
                .write(entity.getRotation())
                .write(entity.getMatch().getTimeElapsed())
                .write(hasTarget);

        if (hasTarget) {
            Player p = (Player)entity;

            write(p.getTarget().x)
                    .write(p.getTarget().y);
        }

        if (isPlayer && id == 0) {
            Player p = (Player)entity;

            write(p.getVisibleIndicatorPosition());
        }
    }
}