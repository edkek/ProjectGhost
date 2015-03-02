package me.eddiep.ghost.server.packet.impl;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.packet.Packet;

import java.io.IOException;
import java.net.DatagramPacket;

public class PositionPacket extends Packet {

    public PositionPacket(Client client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(Client client)  throws IOException {
        int packetNumber = consume(4).asInt();
        if (packetNumber < client.getLastReadPacket()) {
            int dif = client.getLastReadPacket() - packetNumber;
            if (dif >= Integer.MAX_VALUE - 1000) {
                client.setLastReadPacket(packetNumber);
            } else return;
        } else {
            client.setLastReadPacket(packetNumber);
        }

        Player player = client.getPlayer();
        if (player == null)
            return;

        float x = consume(4).asFloat();
        float y = consume(4).asFloat();
        float xvel = consume(4).asFloat();
        float yvel = consume(4).asFloat();

        player.setPosition(x, y);
        player.setVelocity(xvel, yvel);

        if (player.isInMatch()) {
            player.getMatch().positionUpdated(player);
        }
    }

    @Override
    protected void onWritePacket(Client client, Object... args) throws IOException {
        if (args.length != 4)
            return;

        float x = (float)args[0];
        float y = (float)args[1];
        float xvel = (float)args[2];
        float yvel = (float)args[3];
        int lastWrite = client.getLastWritePacket() + 1;
        client.setLastWritePacket(lastWrite);

        DatagramPacket packet =
            write((byte)0x04)
              .write(lastWrite)
              .write(x)
              .write(y)
              .write(xvel)
              .write(yvel)
              .endUDP();

        client.getServer().sendUdpPacket(packet);
    }
}
