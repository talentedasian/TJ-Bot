package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;

public class CreateIdTagCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public CreateIdTagCommand(TagSystem tagSystem) {
        super("createidtag", "Create a tag using a message id", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public CommandData addOptions(CommandData data) {
        return data.addOption(OptionType.STRING, "tag-id", "Tag id", true)
            .addOption(OptionType.NUMBER, "message-id", "Message id", true);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Member member = event.getMember();

        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            String tagId = event.getOption("tag-id").getAsString();
            long messageId = event.getOption("message-id").getAsLong();

            if (tagSystem.exists(tagId)) {
                event.reply("This tag already exists").setEphemeral(true).queue();

                return;
            }

            event.getMessageChannel().retrieveMessageById(messageId).queue(message -> {
                tagSystem.put(tagId, message.getContentRaw());

                event
                    .replyEmbeds(new EmbedBuilder().setColor(Color.GREEN)
                        .setTitle("Success")
                        .setTimestamp(LocalDateTime.now())
                        .setFooter(event.getUser().getAsTag())
                        .setDescription("Successfully created tag '" + tagId + "'")
                        .build())
                    .queue();
            }, failure -> {
                if (failure instanceof ErrorResponseException ex
                        && ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                    event.reply("This message doesn't exist").setEphemeral(true).queue();
                }
            });
        } else {
            event.reply("You need the MESSAGE_MANAGE permission to use this command!")
                .setEphemeral(true)
                .queue();
        }
    }
}
