package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Map;

public class TagsCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public TagsCommand(TagSystem tagSystem) {
        super("tags", "Displays all tags", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.MAGENTA)
            .setTimestamp(LocalDateTime.now())
            .setFooter(event.getUser().getAsTag())
            .setTitle("All available tags");

        for (Map.Entry<String, String> entry : tagSystem.retrieve().entrySet()) {
            String id = entry.getKey(), text = entry.getValue(),
                    preview = text.substring(0, Math.min(text.length(), 50));

            builder.addField(id, preview + (text.length() > 50 ? "..." : ""), true);
        }

        event.replyEmbeds(builder.build()).queue();
    }
}
