package com.boxtrotstudio.ghost.test.game.queue.impl;

import com.boxtrotstudio.ghost.common.game.Player;
import com.boxtrotstudio.ghost.common.game.PlayerFactory;
import com.boxtrotstudio.ghost.game.queue.Queues;
import com.boxtrotstudio.ghost.game.team.Team;
import com.boxtrotstudio.ghost.game.util.VisibleFunction;
import com.boxtrotstudio.ghost.test.game.queue.AbstractPlayerQueue;
import com.boxtrotstudio.ghost.utils.ArrayHelper;
import com.boxtrotstudio.ghost.utils.Global;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TwoVTwoQueue extends AbstractPlayerQueue {
    @Override
    protected List<String> onProcessQueue(List<String> toProcess) {
        List<String> toRemove = new ArrayList<>();

        List<String> queueToProcess = new ArrayList<>(toProcess);

        while (queueToProcess.size() > 3) {
            int player1 = getRandomIndex(queueToProcess.size());
            int player2 = getRandomIndex(queueToProcess.size(), player1);
            int player3 = getRandomIndex(queueToProcess.size(), player1, player2);
            int player4 = getRandomIndex(queueToProcess.size(), player1, player2, player3);

            String id1 = queueToProcess.get(player1);
            String id2 = queueToProcess.get(player2);
            String id3 = queueToProcess.get(player3);
            String id4 = queueToProcess.get(player4);

            Player p1 = PlayerFactory.getCreator().findPlayerByUUID(id1);
            Player p2 = PlayerFactory.getCreator().findPlayerByUUID(id2);
            Player p3 = PlayerFactory.getCreator().findPlayerByUUID(id3);
            Player p4 = PlayerFactory.getCreator().findPlayerByUUID(id4);
            try {
                createMatch(new Team(1, p1, p3), new Team(2, p2, p4));

                queueToProcess.remove(id1);
                queueToProcess.remove(id2);
                queueToProcess.remove(id3);
                queueToProcess.remove(id4);

                toRemove.add(id1);
                toRemove.add(id2);
                toRemove.add(id3);
                toRemove.add(id4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return toRemove;
    }

    private int getRandomIndex(int max, int... toExclude) {
        List<Integer> exclude = new ArrayList<>();
        for (int i : toExclude) {
            exclude.add(i);
        }

        int toReturn;
        do {
            toReturn = Global.RANDOM.nextInt(max);
        } while (exclude.contains(toReturn));

        return toReturn;
    }

    @Override
    public String description() {
        return "Play 2v2";
    }

    @Override
    public Queues queue() {
        return Queues.WEAPONSELECT;
    }

    @Override
    public int allyCount() {
        return 1;
    }

    @Override
    public int opponentCount() {
        return 2;
    }


    @Override
    protected void onTeamEnterMatch(Team team1, Team team2) {
        super.onTeamEnterMatch(team1, team2);

        ArrayHelper.forEach(
                ArrayHelper.combine(team1.getTeamMembers(), team2.getTeamMembers()),
                p -> {
                    p.setLives((byte) 3);
                    p.setVisibleFunction(VisibleFunction.ORIGINAL);
                    p.isVisibleToAllies(true);
                }
        );
    }
}
