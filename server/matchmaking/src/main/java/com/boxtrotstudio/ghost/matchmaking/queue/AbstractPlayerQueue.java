package com.boxtrotstudio.ghost.matchmaking.queue;

import com.boxtrotstudio.ghost.matchmaking.Main;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServer;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.GameServerFactory;
import com.boxtrotstudio.ghost.matchmaking.network.gameserver.Stream;
import com.boxtrotstudio.ghost.matchmaking.player.Player;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerQueue implements PlayerQueue {
    private List<Player> playerQueue = new ArrayList<>();
    private final Stream stream;
    public long queueProcessStart;

    public AbstractPlayerQueue(Stream stream) {
        this.stream = stream;
    }

    @Override
    public final Stream getStream() {
        return stream;
    }

    @Override
    public void addUserToQueue(Player player) {
        if (player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.add(player);
        player.setQueue(this);

        player.getClient().getServer().getLogger().debug(player.getUsername() + " has joined the " + queue().name() + " queue!");
    }

    @Override
    public void removeUserFromQueue(Player player) {
        if (!player.isInQueue() || player.getStream() != stream)
            return;

        playerQueue.remove(player);
        player.setQueue(null);
        player.getClient().getServer().getLogger().debug(player.getUsername() + " has left the " + queue().name() + " queue!");
    }

    private int expand = 1;
    @Override
    public void processQueue() {
        int max = playerQueue.size();
        if (playerQueue.size() >= 100 && expand < 4) {
            max = max / 4;

            max *= expand;
        }

        List<Player> process = new ArrayList<>(playerQueue.subList(0, max));

        queueProcessStart = System.currentTimeMillis();
        List<Player> matches = onProcessQueue(process);

        playerQueue.removeAll(matches);

        if (matches.size() < process.size() / 2) {
            expand++;
        } else {
            expand = 1;
        }
    }

    @Override
    public long getProcessStartTime() {
        return queueProcessStart;
    }

    @Override
    public int playersInQueue() {
        return playerQueue.size();
    }



    protected abstract List<Player> onProcessQueue(List<Player> queueToProcess);

    public boolean createMatch(Player player1, Player player2) throws IOException {
        GameServer server = GameServerFactory.createMatchFor(queue(), new Player[] { player1 }, new Player[] { player2 }, getStream());
        if (server == null) {

            SlackMessage message = new SlackMessage("Queue pop failed! They're no more open servers!");

            SlackAttachment attachment = new SlackAttachment();
            attachment.setText("Queue Type: " + queue().name() + "\nPlayers in Queue: " + playerQueue.size() + "\nGame Servers Online: " + GameServerFactory.getConnectedServers().size());
            attachment.setFallback("Players in Queue: " + playerQueue.size());
            message.addAttachments(attachment);

            Main.SLACK_API.call(message);
            return false;
        }

        player1.setQueue(null);
        player1.setInMatch(true);

        player2.setQueue(null);
        player2.setInMatch(true);

        return true;
    }

    public boolean createMatch(Player[] team1, Player[] team2) throws IOException {
        GameServer server = GameServerFactory.createMatchFor(queue(), team1, team2, getStream());
        if (server == null) {
            SlackMessage message = new SlackMessage("Queue pop failed! They're no more open servers!");

            SlackAttachment attachment = new SlackAttachment();
            attachment.setText("Queue Type: " + queue().name() + "\nPlayers in Queue: " + playerQueue.size() + "\nGame Servers Online: " + GameServerFactory.getConnectedServers().size());
            attachment.setFallback("Players in Queue: " + playerQueue.size());
            message.addAttachments(attachment);

            Main.SLACK_API.call(message);
            return false;
        }

        for (Player p : ArrayHelper.combine(team1, team2)) {
            p.setQueue(null);
            p.setInMatch(true);
        }

        return true;
    }
}
