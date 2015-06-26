package me.eddiep.ghost.network.sql;

import me.eddiep.ghost.game.entities.playable.impl.BaseNetworkPlayer;
import me.eddiep.ghost.game.queue.Queues;
import me.eddiep.ghost.game.ranking.Glicko2;
import me.eddiep.ghost.game.ranking.Rank;
import org.bson.Document;

import java.util.*;

import static me.eddiep.ghost.utils.Constants.*;

public class PlayerData {
    protected String displayname;
    protected String username;
    protected Set<Long> playersKilled = new HashSet<>();
    protected long shotsHit, shotsMissed;

    protected long id;
    protected transient String hash;
    protected transient Rank _rank;
    protected double rank;
    protected long lastRankUpdate;
    protected int hatTricks;
    Set<Long> friends = new HashSet<>();

    public PlayerData(BaseNetworkPlayer p) {
        this.displayname = p.getDisplayName();
        this.username = p.getUsername();
        this.shotsHit = p.getShotsHit();
        this.shotsMissed = p.getShotsMissed();
        this.playersKilled = p.getPlayersKilled();
        this.hatTricks = p.getHatTrickCount();
        this._rank = p.getRanking();
        if (_rank == null)
            _rank = Glicko2.getInstance().defaultRank();
        this.rank = _rank.getRating();
        this.lastRankUpdate = _rank.getLastUpdate();
        this.friends = p.getFriendIds();
    }

    public PlayerData(PlayerData data) {
        this.displayname = data.displayname;
        this.username = data.username;
        this.shotsHit = data.shotsHit;
        this.shotsMissed = data.shotsMissed;
        this.playersKilled = data.playersKilled;
        this.hatTricks = data.hatTricks;
        this._rank = data._rank;
        this.id = data.id;
        this.rank = _rank.getRating();
        this.friends = data.friends;
        this.lastRankUpdate = data.lastRankUpdate;
    }
    
    public PlayerData(String username, String displayname) {
        this(username, displayname, new HashMap<Byte, Integer>(), new HashMap<Byte, Integer>(), 0, 0, new HashSet<Long>(), 0, Glicko2.getInstance().defaultRank(), new HashSet<Long>());
    }
    
    public PlayerData(String username, String displayname, HashMap<Byte, Integer> winHash,
                      HashMap<Byte, Integer> loseHash, long shotsHit, long shotsMissed,
                      Set<Long> playersKilled, int hatTricks, Rank rank, Set<Long> friends) {
        this.username = username;
        this.displayname = displayname;
        this.shotsHit = shotsHit;
        this.shotsMissed = shotsMissed;
        this.playersKilled = playersKilled;
        this.hatTricks = hatTricks;
        this.friends = friends;
        this._rank = rank;
        this.rank = _rank.getRating();
        this.lastRankUpdate = _rank.getLastUpdate();
    }

    public Rank getRank() {
        if (_rank == null)
            _rank = Glicko2.getInstance().defaultRank();

        return _rank;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public Set<Long> getPlayersKilled() {
        return playersKilled;
    }

    public long getId() {
        return id;
    }

    public int getHatTrickCount() {
        return hatTricks;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLastRankUpdate() {
        return lastRankUpdate;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Document asDocument() {
        Document temp = new Document(USERNAME, username)
                .append(DISPLAY_NAME, displayname)
                .append(ID, id)
                .append(HASH, hash)
                .append(SHOTS_HIT, shotsHit)
                .append(SHOTS_MISSED, shotsMissed)
                .append(PLAYERS_KILLED, new ArrayList<>(playersKilled))
                .append(HAT_TRICK, hatTricks)
                .append(RANK, _rank.asDocument())
                .append(FRIENDS, new ArrayList<>(friends));

        /*Document wins = new Document();
        for (Byte t : winHash.keySet()) {
            wins.append(t.toString(), winHash.get(t));
        }

        temp.append(WINS, wins);

        Document loses = new Document();
        for (Byte t : loseHash.keySet()) {
            wins.append(t.toString(), loseHash.get(t));
        }

        temp.append(LOSES, loses);*/

        return temp;
    }

    public static PlayerData fromDocument(Document document) {
        String username = document.getString(USERNAME);

        String displayName = document.getString(DISPLAY_NAME);

        long id = document.getLong(ID);

        long shotsHit = document.getLong(SHOTS_HIT) == null ? 0 : document.getLong(SHOTS_HIT);

        long shotsMissed = document.getLong(SHOTS_MISSED)  == null ? 0 : document.getLong(SHOTS_MISSED);

        List playersKilledList = document.get(PLAYERS_KILLED, List.class);
        HashSet<Long> playersKilled = new HashSet<Long>(playersKilledList);

        int hatTricks = document.getInteger(HAT_TRICK) == null ? 0 : document.getInteger(HAT_TRICK);

        Document rankDoc = document.get("rank", Document.class);
        Rank rank;
        if (rankDoc == null)
            rank = Glicko2.getInstance().defaultRank();
        else
            rank = Rank.fromDocument(rankDoc);

        List friendList = document.get(FRIENDS, List.class);
        if (friendList == null)
            friendList = new ArrayList();

        HashMap<Byte, Integer> wins = new HashMap<>();
        HashMap<Byte, Integer> loses = new HashMap<>();

        Document winDoc = document.get(WINS, Document.class);
        Document loseDoc = document.get(LOSES, Document.class);
        for (Queues type : Queues.values()) {
            if (winDoc.get("" + type.asByte()) != null) {
                wins.put(type.asByte(), winDoc.getInteger("" + type.asByte()));
            }
            if (loseDoc.get("" + type.asByte()) != null) {
                loses.put(type.asByte(), loseDoc.getInteger("" + type.asByte()));
            }
        }

        PlayerData data = new PlayerData(username, displayName, wins, loses, shotsHit, shotsMissed, playersKilled, hatTricks, rank, new HashSet<Long>(friendList));
        data.setId(id);

        return data;
    }

    public long getShotsHit() {
        return shotsHit;
    }

    public long getShotsMissed() {
        return shotsMissed;
    }

    public Set<Long> getFriends() {
        return friends;
    }
}