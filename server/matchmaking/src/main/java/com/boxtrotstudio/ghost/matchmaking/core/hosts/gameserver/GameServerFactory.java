package com.boxtrotstudio.ghost.matchmaking.core.hosts.gameserver;

import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.matchmaking.network.GameServerClient;
import com.boxtrotstudio.ghost.matchmaking.network.packets.GameServerStreamUpdatePacket;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.utils.Global;
import com.google.gson.Gson;

import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class GameServerFactory {
    private static final Gson GSON = Global.GSON;
    private static HashMap<UUID, GameServer> connectedGameServers = new HashMap<>();

    public static boolean isConnected(long id) {
        return connectedGameServers.containsKey(id);
    }

    public static GameServerConfiguration findServerConfig(long id) {
        File file = new File("servers", id + ".gserver");
        if (!file.exists())
            return null;

        try {
            Scanner scanner = new Scanner(file);
            scanner.useDelimiter("\\Z");
            String content = scanner.next();
            scanner.close();

            return GSON.fromJson(content, GameServerConfiguration.class);
        } catch (IOException e) {
            return null;
        }
    }

    public static long getNextID() {
        File file = new File("servers");
        if (!file.exists())
            return 1;

        return file.listFiles((dir, name) -> name.endsWith(".gserver")).length;
    }

    public static void updateServer(long id, String newConfig) {
        //Update file
        File file = new File("servers", id + ".gserver");
        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)))) {
            out.println(newConfig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (connectedGameServers.containsKey(id)) {
            GameServerConfiguration config = GSON.fromJson(newConfig, GameServerConfiguration.class);
            GameServer server = connectedGameServers.get(id);
            server.setConfig(config);
        }
    }

    public static GameServer createFromConfig(GameServerClient client, GameServerConfiguration config) {
        GameServer server = new GameServer(client, config);
        connectedGameServers.put(server.getID(), server);
        return server;
    }

    static void disconnect(GameServer server) {
        connectedGameServers.remove(server.getID());
    }

    public static List<GameServer> getConnectedServers() {
        ArrayList<GameServer> servers = new ArrayList<>();
        for (UUID id : connectedGameServers.keySet()) {
            servers.add(connectedGameServers.get(id));
        }

        return Collections.unmodifiableList(servers);
    }

    public static void bufferServer(GameServer server) {
        server.setStream(Stream.BUFFERED);

        GameServerStreamUpdatePacket packet = new GameServerStreamUpdatePacket(server.getClient());
        try {
            packet.writePacket(Stream.BUFFERED);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<GameServer> getServersWithStream(Stream stream) {
        ArrayList<GameServer> servers = new ArrayList<>();
        for (UUID id : connectedGameServers.keySet()) {
            GameServer server = connectedGameServers.get(id);
            if (server.getConfig().getStream() != stream)
                continue;

            servers.add(server);
        }

        return Collections.unmodifiableList(servers);
    }

    public static GameServer findServerWithIP(InetAddress address) {
        for (UUID id : connectedGameServers.keySet()) {
            GameServer server = connectedGameServers.get(id);
            if (server.getIp().equalsIgnoreCase(address.getHostAddress()))
                return server;
        }
        return null;
    }

    public static GameServer findLeastFullFor(Stream stream) {
        List<GameServer> servers = getServersWithStream(stream);

        GameServer smallest = null;
        for (GameServer server : servers) {
            if (smallest == null) {
                smallest = server;
                continue;
            }
            if (server.getPlayerCount() < smallest.getPlayerCount())
                smallest = server;
        }

        return smallest;
    }

    public static GameServer findLeastFullFor(Stream stream, List<GameServer> exclude) {
        List<GameServer> servers = getServersWithStream(stream);

        GameServer smallest = null;
        for (GameServer server : servers) {
            if (exclude.contains(server))
                continue;

            if (server.isFull())
                continue;

            if (smallest == null) {
                smallest = server;
                continue;
            }
            if (server.getPlayerCount() < smallest.getPlayerCount())
                smallest = server;
        }

        return smallest;
    }

    public static GameServer createMatchFor(Queues queue, Player[] team1, Player[] team2, Stream stream) throws IOException {
        if (stream == Stream.BUFFERED)
            throw new IllegalAccessError("Games cant be created in buffered servers!");

        if (team1[0].getPreferredServer() != null) {
            InetAddress preferred = team1[0].getPreferredServer();
            GameServer preferredServer = findServerWithIP(preferred);
            if (preferredServer != null) {
                try {
                    preferredServer.createMatchFor(queue, team1, team2);
                    return preferredServer;
                } catch (MatchCreationException matchCreationException) {
                    matchCreationException.printStackTrace();
                    return null;
                }
            } else {
                System.err.println("Preferred server is not connected!");
                return null;
            }
        }

        List<GameServer> failed = new ArrayList<>();
        GameServer openServer;
        while (true) {
            openServer = findLeastFullFor(stream, failed);
            if (openServer == null)
                break;
            try {
                openServer.createMatchFor(queue, team1, team2);
            } catch (MatchCreationException matchCreationException) {
                matchCreationException.printStackTrace();
                failed.add(openServer);
                continue;
            }

            break;
        }

        return openServer;
    }

    public static boolean serversFull(Stream stream) {
        return findLeastFullFor(stream) == null;
    }
}
