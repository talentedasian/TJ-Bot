package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;

public class CreateTagCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public CreateTagCommand(TagSystem tagSystem) {
        super("createtag", "Create a tag", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public CommandData addOptions(CommandData data) {
        return data.addOption(OptionType.STRING, "id", "Tag id", true)
            .addOption(OptionType.STRING, "content", "Tag content", true);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Member member = event.getMember();

        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            String tagId = event.getOption("id").getAsString(),
                    content = event.getOption("content").getAsString();

            if (tagSystem.exists(tagId)) {
                event.reply("This tag already exists").setEphemeral(true).queue();

                return;
            }

            tagSystem.put(tagId, content);

            event
                .replyEmbeds(new EmbedBuilder().setColor(Color.GREEN)
                    .setTitle("Success")
                    .setTimestamp(LocalDateTime.now())
                    .setFooter(event.getUser().getAsTag())
                    .setDescription("Successfully created tag '" + tagId + "'")
                    .build())
                .queue();
        } else {
            event.reply("You need the MESSAGE_MANAGE permission to use this command!")
                .setEphemeral(true)
                .queue();
        }
    }
}
