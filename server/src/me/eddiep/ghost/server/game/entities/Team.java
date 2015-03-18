package me.eddiep.ghost.server.game.entities;

import java.util.List;
import java.util.UUID;

public class Team {
    private Player[] members;
    private int teamNumber;

    public Team(int teamNumber, Player... players) {
        members = players;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, UUID... players) {
        Player[] p = new Player[players.length];
        for (int i = 0; i < p.length; i++) {
            Player player;
            if ((player = PlayerFactory.findPlayerByUUID(players[i])) == null) {
                throw new IllegalArgumentException("Invalid UUID!");
            }

            p[i] = player;
        }

        this.members = p;
        this.teamNumber = teamNumber;
    }

    public Team(int teamNumber, List<Player> players) {
        members = players.toArray(new Player[players.size()]);
        this.teamNumber = teamNumber;
    }

    public boolean isTeamDead() {
        for (Player p : members) {
            if (!p.isDead())
                return false;
        }
        return true;
    }

    public boolean isTeamAlive() {
        return !isTeamDead();
    }

    public int getTeamLength() {
        return members.length;
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public Player[] getTeamMembers() {
        return members;
    }

    public boolean isAlly(Player p) {
        for (Player member : members) {
            if (p.getSession().equals(member.getSession()))
                return true;
        }
        return false;
    }

    public boolean isTeamReady() {
        for (Player p : members) {
            if (!p.isReady())
                return false;
        }
        return true;
    }

    private OfflineTeam offlineTeam;
    public OfflineTeam offlineTeam() {
        if (offlineTeam == null)
            offlineTeam = new OfflineTeam(this);
        return offlineTeam;
    }
}