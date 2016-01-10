package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.utils.Vector2f;

import java.io.IOException;

public class ActionRequestPacket extends Packet<BaseServer, BasePlayerClient> {
    public ActionRequestPacket(BasePlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(BasePlayerClient client)  throws IOException {
        int packetNumber = consume(4).asInt();
        if (packetNumber < client.getLastReadPacket()) {
            int dif = client.getLastReadPacket() - packetNumber;
            if (dif >= Integer.MAX_VALUE - 1000) {
                client.setLastReadPacket(packetNumber);
            } else return;
        } else {
            client.setLastReadPacket(packetNumber);
        }

        byte actionType = consume(1).asByte();
        float mouseX = consume(4).asFloat();
        float mouseY = consume(4).asFloat();
        //long time = consume(4).asLong();

        if (actionType == 0)
            client.getPlayer().moveTowards(mouseX, mouseY);
        else if (actionType == 1)
            client.getPlayer().fireTowards(mouseX, mouseY, actionType);
        else if (actionType == 2) {
            Vector2f direction = new Vector2f(mouseX, mouseY);

            if (direction.length() != 0f)
                direction.normalise();

            PlayableEntity p = client.getPlayer();
            client.getPlayer().setTarget(null);
            client.getPlayer().setVelocity(direction.x * p.getSpeed(), direction.y * p.getSpeed());
        }
        else
            System.err.println("[SERVER] Unknown action " + actionType + " ! (" + client.getIpAddress() + ")");
    }
}
