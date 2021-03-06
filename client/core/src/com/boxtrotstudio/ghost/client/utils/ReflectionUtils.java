package com.boxtrotstudio.ghost.client.utils;

import java.lang.reflect.Field;

public class ReflectionUtils {

    public static <T> T getField(Object object, String fieldName) {
        try {
            Field f = object.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            return (T) f.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
