package org.togetherjava.tjbot.tag;

import org.togetherjava.tjbot.db.Database;

public class TagSystemInstanceHolder {
    private TagSystemInstanceHolder() {}

    private static TagSystem instance;

    public static TagSystem getOrCreate(Database database) {
        if (instance == null) {
            setInstance(new TagSystem(database));
        }

        return instance;
    }

    public static TagSystem getInstance() {
        if (instance == null) {
            throw new UnsupportedOperationException("Instance not yet set");
        }

        return instance;
    }

    public static void setInstance(TagSystem tagSystem) {
        if (instance != null) {
            throw new UnsupportedOperationException("Instance already set");
        }

        instance = tagSystem;
    }
}
