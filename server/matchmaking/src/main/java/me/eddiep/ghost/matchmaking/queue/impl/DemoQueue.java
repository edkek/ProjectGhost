package me.eddiep.ghost.matchmaking.queue.impl;

import me.eddiep.ghost.matchmaking.network.gameserver.Stream;
import me.eddiep.ghost.matchmaking.player.Player;
import me.eddiep.ghost.matchmaking.queue.AbstractPlayerQueue;
import me.eddiep.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DemoQueue extends AbstractPlayerQueue {
    public DemoQueue(Stream stream) {
        super(stream);
    }

    @Override
    protected List<Player> onProcessQueue(List<Player> toProcess) {
        List<Player> toRemove = new ArrayList<>();

        List<Player> queueToProcess = new ArrayList<>(toProcess);

        while (queueToProcess.size() > 1) {
            int randomIndex = Global.RANDOM.nextInt(queueToProcess.size());
            int randomIndex2;
            do {
                randomIndex2 = Global.RANDOM.nextInt(queueToProcess.size());
            } while (randomIndex2 == randomIndex);

            Player player1 = queueToProcess.get(randomIndex);
            Player player2 = queueToProcess.get(randomIndex2);

            if (getStream() == Stream.TEST) {
                if (player1.getPreferedServer() != null || player2.getPreferedServer() != null) {
                    if (player1.getPreferedServer() != player2.getPreferedServer())
                        continue;
                }
            }

            try {
                if (!createMatch(player1, player2))
                    continue;

                int toRemove2 = randomIndex2 > randomIndex ? randomIndex2 - 1 : randomIndex2;

                queueToProcess.remove(randomIndex);
                queueToProcess.remove(toRemove2);

                toRemove.add(player1);
                toRemove.add(player2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }
}
