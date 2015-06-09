package me.eddiep.ghost.server.network.packet;

import me.eddiep.ghost.server.network.Client;
import me.eddiep.ghost.server.network.packet.impl.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class PacketFactory {
    private static HashMap<Byte, Class<? extends Packet>> packets = new HashMap<>();

    static {
        packets.put((byte) 0x01, OkPacket.class);
        packets.put((byte) 0x02, MatchFoundPacket.class);
        packets.put((byte) 0x03, ReadyPacket.class);
        packets.put((byte) 0x04, EntityStatePacket.class);
        packets.put((byte) 0x05, QueueRequestPacket.class);
        packets.put((byte) 0x08, ActionRequestPacket.class);
        packets.put((byte) 0x09, PingPongPacket.class);
        packets.put((byte) 0x10, SpawnEntityPacket.class);
        packets.put((byte) 0x11, DespawnEntityPacket.class);
        packets.put((byte) 0x12, PlayerStatePacket.class);
        //TODO Packet 0x13 - ???
        packets.put((byte) 0x14, SetDisplayNamePacket.class);
        packets.put((byte) 0x15, NewNotificationPacket.class);
        packets.put((byte) 0x16, DeleteRequestPacket.class);
        packets.put((byte) 0x17, RespondRequestPacket.class);
        //TODO Packet 0x18 - PrivateMatchReady Packet
        packets.put((byte) 0x19, TcpPingPongPacket.class);
        packets.put((byte) 0x20, LeaveQueuePacket.class);
    }

    public static Packet get(byte opCode, Client client) {
        try {
            return packets.get(opCode).getConstructor(Client.class).newInstance(client);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Packet get(byte opCode, Client client, byte[] data) {
        try {
            return packets.get(opCode).getConstructor(Client.class, byte[].class).newInstance(client, data);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}
