package com.boxtrotstudio.ghost.common.network.netty;

import com.boxtrotstudio.ghost.common.network.packet.PlayerPacketFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteOrder;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> objects) throws Exception {
        if (byteBuf.readableBytes() == 0)
            return;

        byte opCode = byteBuf.getByte(0);
        int packetSize;
        if (opCode == 0) {
            byteBuf = byteBuf.order(ByteOrder.LITTLE_ENDIAN);
            short sessionLength = byteBuf.getShort(1);
            packetSize = 2 + sessionLength + 1;
        } else {
            packetSize = PlayerPacketFactory.packetSize(opCode) + 1;
        }

        if (opCode == -1) {
            System.err.println("Unknown op code: " + opCode);
        }

        if (byteBuf.readableBytes() < packetSize)
            return;

        byte[] packet = new byte[packetSize];
        byteBuf.readBytes(packetSize).getBytes(0, packet);

        objects.add(packet);
    }
}
