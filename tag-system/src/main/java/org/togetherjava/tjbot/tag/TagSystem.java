package org.togetherjava.tjbot.tag;

import org.togetherjava.tjbot.db.Database;
import org.togetherjava.tjbot.db.generated.tables.Tags;
import org.togetherjava.tjbot.db.generated.tables.records.TagsRecord;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author illuminator3
 */
public final class TagSystem {
    private final Database database;

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
        });
    }

    public void put(String tag, String content) {
        database.writeTransaction(ctx -> {
            ctx.insertInto(Tags.TAGS, Tags.TAGS.ID, Tags.TAGS.CONTENT)
                .values(tag, content)
                .onDuplicateKeyUpdate()
                .set(Tags.TAGS.CONTENT, content)
                .execute();
        });
    }

    public String get(String tag) {
        return database.readTransaction(ctx -> {
            return Optional
                .ofNullable(ctx.selectFrom(Tags.TAGS).where(Tags.TAGS.ID.eq(tag)).fetchOne())
                .map(TagsRecord::getContent)
                .orElse(null);
        });
    }

    public Map<String, String> retrieve() {
        return database.readTransaction(ctx -> {
            return ctx.selectFrom(Tags.TAGS)
                .fetch()
                .stream()
                .collect(Collectors.toMap(TagsRecord::getId, TagsRecord::getContent));
        });
    }

    public Set<String> retrieveIds() {
        return Collections.unmodifiableSet(retrieve().keySet());
    }
}
