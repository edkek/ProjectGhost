package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class RespondRequestPacket extends Packet<TcpServer, PlayerClient> {
    public RespondRequestPacket(PlayerClient client) {
        super(client);
    }

    @Override
    public void onHandlePacket(PlayerClient client)  throws IOException {
        int id = consume(4).asInt();
        boolean value = consume(1).asBoolean();

        if (client.getUser() != null) {
            client.getPlayer().respondToRequest(id, value);
        }
    }
}