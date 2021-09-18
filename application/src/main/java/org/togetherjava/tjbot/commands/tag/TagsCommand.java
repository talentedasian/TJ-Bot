package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

public class TagsCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public TagsCommand(TagSystem tagSystem) {
        super("tags", "Displays all tags", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        event
            .replyEmbeds(TagUtility.buildAllTagsEmbed(event.getUser().getAsTag(), tagSystem)
                .setTitle("All available tags")
                .build())
            .queue();
    }
}
