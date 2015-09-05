package me.eddiep.ghost.common.network.packet;

import me.eddiep.ghost.common.network.BasePlayerClient;
import me.eddiep.ghost.common.network.BaseServer;
import me.eddiep.ghost.network.packet.Packet;

import java.io.IOException;

public class MatchFoundPacket extends Packet<BaseServer, BasePlayerClient> {
    public MatchFoundPacket(BasePlayerClient client) {
        super(client);
    }

    @Override
    protected void onWritePacket(BasePlayerClient client, Object... args) throws IOException{
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