package org.togetherjava.tjbot.tag;

import org.togetherjava.tjbot.db.Database;

/**
 * Holds the instance of the current {@link TagSystem}
 */
public final class TagSystemInstanceHolder {
    private TagSystemInstanceHolder() {}

    private static volatile TagSystem instance;

    public static TagSystem getOrCreate(Database database) {
        if (instance == null) {
            setInstance(new TagSystem(database));
        }

        return instance;
    }

    public static TagSystem getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Instance not yet set");
        }

        return instance;
    }

    public static void setInstance(TagSystem tagSystem) {
        if (instance != null) {
            throw new IllegalStateException("Instance already set");
        }

        instance = tagSystem;
    }
}
