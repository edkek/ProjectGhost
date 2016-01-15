package me.eddiep.ghost.client;

import me.eddiep.ghost.client.core.Logical;

import java.util.ArrayList;

public class LogicHandler {
    private long _tickStart = 0L;
    private long ntick;
    private long ms;
    private int updates = 0;

    private final ArrayList<Logical> logicals = new ArrayList<>();
    private final ArrayList<Logical> logicsToAdd = new ArrayList<>();
    private final ArrayList<Logical> toRemove = new ArrayList<>();
    private boolean isLogicLooping = false;

    public void init() {
        _tickStart = System.currentTimeMillis();
        ntick = getTickCount();
        ms = getTickCount();
    }

    public long getTickCount() {
        return System.currentTimeMillis() - _tickStart;
    }

    public void tick(Handler handler) {
        int loop = 0;
        while (getTickCount() > ntick && loop < 60) {
            _tick(handler);

            ntick += (1000L / (1000L / 17L));
            loop++;
            updates++;
            if (getTickCount() - ms < 1000) {
                continue;
            }
            updates = 0;
            ms = getTickCount();
        }
    }

    private void _tick(Handler handler) {
        //Tick the current handler
        handler.tick();

        //Loop through any logic
        isLogicLooping = true;
        for (Logical l : logicals) {
            l.tick();
        }
        isLogicLooping = false;

        //Update logic array
        logicals.addAll(logicsToAdd);
        logicsToAdd.clear();
        for (Logical l : toRemove) {
            logicals.remove(l);
            l.dispose();
        }
        toRemove.clear();
    }

    public void addLogical(Logical logic) {
        if (isLogicLooping)
            logicsToAdd.add(logic);
        else
            logicals.add(logic);
    }

    public void removeLogical(Logical logic) {
        if (isLogicLooping)
            toRemove.add(logic);
        else {
            logicals.remove(logic);

            logic.dispose();
        }
    }

    public void clear() {
        logicals.clear();
        toRemove.clear();
        logicsToAdd.clear();
    }
}
