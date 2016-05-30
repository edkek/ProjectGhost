package com.boxtrotstudio.ghost.gameserver.api.network.impl;

import com.boxtrotstudio.ghost.common.game.MatchCreator;
import com.boxtrotstudio.ghost.common.game.NetworkMatch;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.common.network.BaseServer;
import com.boxtrotstudio.ghost.common.network.world.NetworkWorld;
import com.boxtrotstudio.ghost.game.match.Match;
import com.boxtrotstudio.ghost.game.match.entities.PlayableEntity;
import com.boxtrotstudio.ghost.game.match.stats.MatchHistory;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.gameserver.api.GameServer;
import com.boxtrotstudio.ghost.gameserver.api.Stream;
import com.boxtrotstudio.ghost.gameserver.api.game.Game;
import com.boxtrotstudio.ghost.gameserver.api.network.packets.MatchHistoryPacket;
import com.boxtrotstudio.ghost.gameserver.common.GameFactory;
import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.utils.ArrayHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class BasicMatchFactory implements MatchCreator {
    private HashMap<Long, NetworkMatch> activeMatches = new HashMap<>();

    @Override
    public NetworkMatch createMatchFor(Team team1, Team team2, long id, Queues queue, String map, BaseServer server) throws IOException {
        Game game = GameFactory.getGameFor(queue);
        map = game.getMapName();

        NetworkMatch match = new NetworkMatch(team1, team2, server);
        NetworkWorld world = new NetworkWorld(map, match);
        match.setQueueType(queue);
        match.setWorld(world);
        game.onMatchPreSetup(match);
        match.setup();
        game.onMatchSetup(match);
        match.setID(id);

        activeMatches.put(match.getID(), match);

        return match;
    }

    @Override
    public void endAndSaveMatch(NetworkMatch match) {
        activeMatches.remove(match.getID());

        saveMatchInfo(match.matchHistory(), match.disconnectdPlayers);

        //Invalidate all players in this match
        for (PlayableEntity playable : ArrayHelper.combine(match.getTeam1().getTeamMembers(), match.getTeam2().getTeamMembers())) {
            if (playable instanceof Player) {
                Player player = (Player)playable;

                PlayerFactory.getCreator().invalidateSession(player);
            }
        }

        match.dispose();

        if (GameServer.currentStream == Stream.BUFFERED) {
            if (activeMatches.size() == 0) {
                System.err.println("All matches finished, stopping server..");
                try {
                    GameServer.stopServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.exit(0);
            }
        }
    }

    public void saveMatchInfo(MatchHistory match, List<Player> disconnects) {
        MatchHistoryPacket packet = new MatchHistoryPacket(GameServer.getMatchmakingClient());
        try {
            packet.writePacket(match, disconnects);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to save match!");
        }
    }

    @Override
    public Match findMatch(long id) {
        if (activeMatches.containsKey(id))
            return activeMatches.get(id);
        else {
            return null;
        }
    }

    @Override
    public List<NetworkMatch> getAllActiveMatches() {
        ArrayList<NetworkMatch> matches = new ArrayList<>();

        for (Long id : activeMatches.keySet()) {
            matches.add(activeMatches.get(id));
        }

        return Collections.unmodifiableList(matches);
    }

    @Override
    public void createMatchFor(NetworkMatch match, long id, Queues queue, String mapName, BaseServer server) {
        throw new IllegalAccessError("Not implemented!");
    }
}
