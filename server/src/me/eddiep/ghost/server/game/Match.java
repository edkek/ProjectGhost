package me.eddiep.ghost.server.game;

import me.eddiep.ghost.server.Main;
import me.eddiep.ghost.server.TcpUdpServer;
import me.eddiep.ghost.server.game.queue.QueueType;
import me.eddiep.ghost.server.game.util.Vector2f;
import me.eddiep.ghost.server.network.Player;
import me.eddiep.ghost.server.packet.impl.MatchFoundPacket;
import me.eddiep.ghost.server.packet.impl.PositionPacket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Match {
    /*private Team team1;
    private Team team2;*/
    private Player player1;
    private Player player2;
    private TcpUdpServer server;
    private boolean started;

    public Match(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.server = this.player1.getClient().getServer();
    }

/*
    public Team createTeam1(Player... players) {
        team1 = new Team(1, players);
        return team1;
    }

    public Team createTeam2(Player... players) {
        team2 = new Team(2, players);
        return team2;
    }*/

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    private void start() {
        started = true;

        player1.setReady(false);
        player2.setReady(false);

        player1.setMatch(this);
        player2.setMatch(this);
    }

    public void tick() {
        if (!started) {
            if (player1.isReady() && player2.isReady()) {
                start();
                return;
            }
        }

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                tick();
            }
        });
    }

    public void setup() throws IOException {
        short p1X = (short)Main.random(-504, 0);
        short p1Y = (short)Main.random(-350, 350);

        short p2X = (short)Main.random(0, 504);
        short p2Y = (short)Main.random(-350, 350);

        MatchFoundPacket packet1 = new MatchFoundPacket(player1.getClient());
        MatchFoundPacket packet2 = new MatchFoundPacket(player2.getClient());

        packet1.writePacket(player2.getClient(), p1X, p1Y, p2X, p2Y);
        packet2.writePacket(player1.getClient(), p2X, p2Y, p1X, p1Y);

        server.executeNextTick(new Runnable() {
            @Override
            public void run() {
                start();
            }
        });
    }

    public void positionUpdated(Player player) throws IOException {
        PositionPacket packet;
        if (player.equals(player1)) {
            packet = new PositionPacket(player2.getClient(), new byte[0]); //No data to read
        } else if (player.equals(player2)) {
            packet = new PositionPacket(player1.getClient(), new byte[0]); //No data to read
        } else return;

        Vector2f position = player.getPosition();
        Vector2f velocity = player.getVelocity();

        packet.writePacket(position.x, position.y, velocity.x, velocity.y);
    }
}