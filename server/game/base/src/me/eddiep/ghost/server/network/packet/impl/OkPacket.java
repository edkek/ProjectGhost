package me.eddiep.ghost.server.network.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.Packet;

import java.io.IOException;

public class OkPacket extends Packet {
    public OkPacket(Client client) {
        super(client);
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length == 0)
            return;

        boolean isOk = (boolean)args[0];

        write((byte)0x01)
                .write(isOk)
                .endTCP();
    }
}
