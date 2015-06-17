package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.game.entities.playable.Playable;
import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class PlayerStatePacket extends Packet {
    public PlayerStatePacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 1)
            return;

        Playable player = (Playable)args[0];

        write((byte)0x12)
            .write(client.getPlayer().equals(player) ? (short)0 : player.getEntity().getID())
            .write(player.getLives())
            .write(player.isDead())
            .write(player.isFrozen())
            .endTCP();
    }
}
