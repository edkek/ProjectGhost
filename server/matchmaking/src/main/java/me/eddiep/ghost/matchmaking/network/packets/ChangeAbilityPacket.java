package me.eddiep.ghost.matchmaking.network.packets;

import me.eddiep.ghost.game.match.abilities.*;
import me.eddiep.ghost.matchmaking.network.PlayerClient;
import me.eddiep.ghost.matchmaking.network.TcpServer;
import me.eddiep.ghost.network.packet.Packet;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;

public class ChangeAbilityPacket extends Packet<TcpServer, PlayerClient> {
    public static final Class[] WEAPONS = new Class[] {
            Gun.class,
            Laser.class,
            Circle.class,
            Dash.class,
            Boomerang.class
    };

    public ChangeAbilityPacket(PlayerClient client, byte[] data) {
        super(client, data);
    }

    @Override
    public void onHandlePacket(PlayerClient client) throws IOException {
        byte action = consume(1).asByte();

        if (client.getPlayer() == null)
            return;


        if (action == 0x10) {
            client.getPlayer().setCurrentAbility(WEAPONS[Global.RANDOM.nextInt(WEAPONS.length)]);
        } else {
            client.getPlayer().setCurrentAbility(WEAPONS[action - 1]);
        }
    }
}
