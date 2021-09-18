package org.togetherjava.tjbot.tag;

import org.jooq.Result;
import org.togetherjava.tjbot.db.Database;
import org.togetherjava.tjbot.db.generated.tables.Tags;
import org.togetherjava.tjbot.db.generated.tables.records.TagsRecord;

import java.util.*;

public class TagSystem {
    private final Database database;
    private boolean changed = true;
    private Map<String, String> cache;

    public TagSystem(Database database) {
        this.database = database;
    }

    public boolean exists(String tag) {
        return database.readTransaction(ctx -> {
            return Optional
                .ofNullable(ctx.selectFrom(Tags.TAGS).where(Tags.TAGS.ID.eq(tag)).fetchOne())
                .isPresent();
        });
    }

    public void delete(String tag) {
        database.writeTransaction(ctx -> {
            ctx.deleteFrom(Tags.TAGS).where(Tags.TAGS.ID.eq(tag)).execute();

            changed = true;
        });
    }

    public void put(String tag, String content) {
        database.writeTransaction(ctx -> {
            TagsRecord tagsRecord = ctx.newRecord(Tags.TAGS).setId(tag).setContent(content);

            if (tagsRecord.update() == 0) {
                tagsRecord.insert();
            }

            changed = true;
        });
    }

    public String get(String tag) {
        if (!changed) {
            return cache.get(tag);
        }

        retrieve();

        return get(tag);
    }

    public Map<String, String> retrieve() {
        if (!changed) {
            return cache;
        }

        return database.readTransaction(ctx -> {
            Result<TagsRecord> result = ctx.selectFrom(Tags.TAGS).fetch();
            Map<String, String> out = new HashMap<>();

            for (TagsRecord tagsRecord : result) {
                out.put(tagsRecord.getId(), tagsRecord.getContent());
            }

            cache = out;
            changed = false;

            return out;
        });
    }

    public Set<String> retrieveIds() {
        return retrieve().keySet();
    }
}
