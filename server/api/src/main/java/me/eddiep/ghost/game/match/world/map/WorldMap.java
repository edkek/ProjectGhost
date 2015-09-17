package me.eddiep.ghost.game.match.world.map;

import me.eddiep.ghost.utils.Global;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class WorldMap {
    private String name;
    private String backgroundTexture;
    private EntityLocations[] locations;
    private float ambiantPower;
    private AmbiantColor ambiantColor;

    public static WorldMap fromFile(File file) throws FileNotFoundException {
        if (!file.exists())
            return null;

        Scanner scanner = new Scanner(file);
        scanner.useDelimiter("\\Z");
        String content = scanner.next();
        scanner.close();

        return Global.GSON.fromJson(content, WorldMap.class);
    }

    private WorldMap() { }

    public String getName() {
        return name;
    }

    public String getBackgroundTexture() {
        return backgroundTexture;
    }

    public float getAmbiantPower() {
        return ambiantPower;
    }

    public int[] getAmbiantColor() {
        return new int[] {
                ambiantColor.red,
                ambiantColor.green,
                ambiantColor.blue
        };
    }

    public EntityLocations[] getStartingLocations() {
        return locations;
    }

    public void dispose() {
        locations = null;
        name = null;
        backgroundTexture = null;
    }

    public class EntityLocations {
        private short id;
        private float x;
        private float y;
        private double rotation;

        public short getId() {
            return id;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public double getRotation() {
            return rotation;
        }
    }

    public class AmbiantColor {
        private int red;
        private int green;
        private int blue;
    }
}
