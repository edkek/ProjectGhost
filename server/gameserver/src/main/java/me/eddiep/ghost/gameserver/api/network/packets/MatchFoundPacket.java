package me.eddiep.ghost.gameserver.api.network.packets;

import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.gameserver.api.network.TcpUdpClient;
import me.eddiep.ghost.gameserver.api.network.TcpUdpServer;

import java.io.IOException;

public class MatchFoundPacket extends Packet<TcpUdpServer, TcpUdpClient> {
    public MatchFoundPacket(TcpUdpClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(TcpUdpClient client, Object... args) throws IOException{
        if (args.length != 2)
            return;

        float startX = (float)args[0];
        float startY = (float)args[1];

        write((byte)0x02)
                .write(startX)
                .write(startY)
                .endTCP();
    }
}
