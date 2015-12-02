package me.eddiep.ghost.test.game.queue;

import me.eddiep.ghost.common.game.MatchFactory;
import me.eddiep.ghost.common.game.NetworkMatch;
import me.eddiep.ghost.common.game.Player;
import me.eddiep.ghost.common.game.PlayerFactory;
import me.eddiep.ghost.game.match.Match;
import me.eddiep.ghost.game.match.entities.PlayableEntity;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.team.Team;
import me.eddiep.ghost.test.Main;
import me.eddiep.ghost.test.game.TestPlayer;
import me.eddiep.ghost.utils.ArrayHelper;
import me.eddiep.ghost.utils.Global;
import me.eddiep.ghost.utils.PFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<String> playerQueue = new ArrayList<>();
    private static final HashMap<Queues, ArrayList<Long>> matches = new HashMap<Queues, ArrayList<Long>>();

    static {
        for (Queues t : Queues.values()) {
            matches.put(t, new ArrayList<Long>());
        }
    }

    public static List<Long> getMatchesFor(Queues type) {
        return Collections.unmodifiableList(matches.get(type));
    }

    @Override
    public void addUserToQueue(TestPlayer player) {
        if (player.isInQueue())
            return;

        playerQueue.add(player.getSession());
        player.setQueue(this);

        System.out.println("[SERVER] " + player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(TestPlayer player) {
        if (!player.isInQueue())
            return;

        playerQueue.remove(player.getSession());
        player.setQueue(null);
        System.out.println("[SERVER] " + player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100) {
            max = max / 4;
        }

        List<String> process = playerQueue.subList(0, max);

        playerQueue.removeAll(onProcessQueue(process));
    }

    @Override
    public QueueInfo getInfo() {
        long playersInMatch = 0;
        ArrayList<Long> matchIds = matches.get(queue());
        for (long id : matchIds) {
            Match match = MatchFactory.getCreator().findMatch(id);
            playersInMatch += match.team1().getTeamLength() + match.team2().getTeamLength();
        }

        return new QueueInfo(queue(), playerQueue.size(), playersInMatch, description(), allyCount(), opponentCount());
    }

    protected abstract List<String> onProcessQueue(List<String> queueToProcess);

    public void createMatch(String user1, String user2) throws IOException {
        Player player1 = PlayerFactory.getCreator().findPlayerByUUID(user1);
        Player player2 = PlayerFactory.getCreator().findPlayerByUUID(user2);
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();

        Team team1 = new Team(1, player1);
        Team team2 = new Team(2, player2);

        NetworkMatch match = MatchFactory.getCreator().createMatchFor(team1, team2, id, queue(), "test", Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());
    }

    public void createMatch(Team team1, Team team2) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();
        Match match = MatchFactory.getCreator().createMatchFor(team1, team2, id, queue(), "test", Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(team1, team2);

        ArrayHelper.assertTrueFor(team1.getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    public void createMatch(NetworkMatch match) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();
        MatchFactory.getCreator().createMatchFor(match, id, queue(), "test", Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());

        ArrayHelper.assertTrueFor(match.getTeam1().getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    public void createMatch(NetworkMatch match, String map) throws IOException {
        long id = Global.SQL.getStoredMatchCount() + MatchFactory.getCreator().getAllActiveMatches().size();
        MatchFactory.getCreator().createMatchFor(match, id, queue(), map, Main.TCP_UDP_SERVER);

        matches.get(queue()).add(match.getID());

        onTeamEnterMatch(match.getTeam1(), match.getTeam2());

        ArrayHelper.assertTrueFor(match.getTeam1().getTeamMembers(), new PFunction<PlayableEntity, Boolean>() {
            @Override
            public Boolean run(PlayableEntity p) {
                return p instanceof TestPlayer && ((TestPlayer) p).getQueue() == null;
            }
        }, "super.onTeamEnterMatch was not invoked!");
    }

    protected void onTeamEnterMatch(Team team1, Team team2) {
        for (PlayableEntity p : team1.getTeamMembers()) {
            if (p instanceof TestPlayer)
                ((TestPlayer)p).setQueue(null);
        }

        for (PlayableEntity p : team2.getTeamMembers()) {
            if (p instanceof TestPlayer)
                ((TestPlayer)p).setQueue(null);
        }
    }
}
