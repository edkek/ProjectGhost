package com.boxtrotstudio.ghost.client.core.game.timeline;

import java.util.Arrays;

public class WorldSnapshot {
    private long snapshotTaken;
    private EntitySnapshot[] entitySnapshots;
    private EntityDespawnSnapshot[] entityDespawnSnapshots;
    private EntitySpawnSnapshot[] entitySpawnSnapshots;
    private PlayableSnapshot[] playableUpdates;
    private EventSnapshot[] events;

    private WorldSnapshot() { }

    public long getSnapshotTaken() {
        return snapshotTaken;
    }

    public EntitySnapshot[] getEntitySnapshots() {
        return entitySnapshots;
    }

    public EntityDespawnSnapshot[] getEntityDespawnSnapshots() {
        return entityDespawnSnapshots;
    }

    public EntitySpawnSnapshot[] getEntitySpawnSnapshots() {
        return entitySpawnSnapshots;
    }

    public PlayableSnapshot[] getPlayableChanges() {
        return playableUpdates;
    }

    public EventSnapshot[] getEvents() {
        return events;
    }

    @Override
    public String toString() {
        return "WorldSnapshot{" +
                "snapshotTaken=" + snapshotTaken +
                ", entitySnapshots=" + Arrays.toString(entitySnapshots) +
                ", entityDespawnSnapshots=" + Arrays.toString(entityDespawnSnapshots) +
                ", entitySpawnSnapshots=" + Arrays.toString(entitySpawnSnapshots) +
                ", playableUpdates=" + Arrays.toString(playableUpdates) +
                ", events=" + Arrays.toString(events) +
                '}';
    }
}
