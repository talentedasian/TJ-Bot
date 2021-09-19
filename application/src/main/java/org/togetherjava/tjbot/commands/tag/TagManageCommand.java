package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class TagManageCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public TagManageCommand(TagSystem tagSystem) {
        super("tagmanage", "Manage the tags", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public @NotNull CommandData addOptions(@NotNull CommandData commandData) {
        return commandData.addSubcommands(
                new SubcommandData("rawtag", "View a tag in its raw form")
                    .addOption(OptionType.STRING, "id", "tag id"),
                new SubcommandData("edittag", "Edit a tag")
                    .addOption(OptionType.STRING, "id", "tag id", true)
                    .addOption(OptionType.STRING, "content", "Tag content", true),
                new SubcommandData("editidtag", "Edit a tag using a message id")
                    .addOption(OptionType.STRING, "tag-id", "tag id", true)
                    .addOption(OptionType.NUMBER, "message-id", "Message id", true),
                new SubcommandData("deletetag", "Delete a tag").addOption(OptionType.STRING, "id",
                        "tag id", true),
                new SubcommandData("createtag", "Create a tag")
                    .addOption(OptionType.STRING, "id", "tag id", true)
                    .addOption(OptionType.STRING, "content", "Tag content", true),
                new SubcommandData("createidtag", "Create a tag using a message id")
                    .addOption(OptionType.STRING, "tag-id", "tag id", true)
                    .addOption(OptionType.NUMBER, "message-id", "Message id", true));
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Member member = event.getMember();

        if (!member.hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply("You need the MESSAGE_MANAGE permission to use this command!")
                .setEphemeral(true)
                .queue();

            return;
        }

        switch (event.getSubcommandName()) {
            case "edittag" -> editTag(event);
            case "editidtag" -> editIdTag(event);
            case "deletetag" -> deleteTag(event, member);
            case "createtag" -> createTag(event);
            case "createidtag" -> createIdTag(event);
            default -> throw new IllegalStateException();
        }
    }

    private void editTag(SlashCommandEvent event) {
        String tagId = event.getOption("id").getAsString();
        String content = event.getOption("content").getAsString();

        if (!tagSystem.exists(tagId)) {
            event.reply("This tag does not exist").setEphemeral(true).queue();

            return;
        }

        tagSystem.put(tagId, content);

        event
            .replyEmbeds(new EmbedBuilder().setColor(Color.GREEN)
                .setTitle("Success")
                .setTimestamp(LocalDateTime.now())
                .setFooter(event.getUser().getAsTag())
                .setDescription("Successfully edited tag '" + tagId + "'")
                .build())
            .queue();
    }

    private void editIdTag(SlashCommandEvent event) {
        String tagId = event.getOption("tag-id").getAsString();
        long messageId = event.getOption("message-id").getAsLong();

        if (!tagSystem.exists(tagId)) {
            event.reply("This tag does not exist").setEphemeral(true).queue();

            return;
        }

        event.getMessageChannel().retrieveMessageById(messageId).queue(message -> {
            tagSystem.put(tagId, message.getContentRaw());

            event
                .replyEmbeds(new EmbedBuilder().setColor(Color.GREEN)
                    .setTitle("Success")
                    .setTimestamp(LocalDateTime.now())
                    .setFooter(event.getUser().getAsTag())
                    .setDescription("Successfully edited tag '" + tagId + "'")
                    .build())
                .queue();
        }, failure -> {
            if (failure instanceof ErrorResponseException ex
                    && ex.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                event.reply("This message doesn't exist").setEphemeral(true).queue();
            } else {
                failure.printStackTrace();
            }
        });
    }

    private void deleteTag(SlashCommandEvent event, Member member) {
        String tagId = event.getOption("id").getAsString();

        if (!tagSystem.exists(tagId)) {
            event.reply("This tag does not exist").setEphemeral(true).queue();

            return;
        }

        event.reply("You sure? Confirming this will delete the tag '" + tagId + "'")
            .addActionRow(
                    Button.of(ButtonStyle.DANGER, generateComponentId(member.getId(), tagId),
                            "Of course!"),
                    Button.of(ButtonStyle.SUCCESS, generateComponentId(member.getId()), "Abort"))
            .queue();
    }

    private void createTag(SlashCommandEvent event) {
        String tagId = event.getOption("id").getAsString();
        String content = event.getOption("content").getAsString();

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
    }

    private void createIdTag(SlashCommandEvent event) {
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
            } else {
                failure.printStackTrace();
            }
        });
    }

    @Override
    public void onButtonClick(ButtonClickEvent event, List<String> idArgs) {
        String userId = idArgs.get(0);

        if (!event.getUser().getId().equals(userId)
                && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.reply(":police_car: Button theft is not allowed").setEphemeral(true).queue();

            return;
        }

        if (event.getButton().getLabel().equals("Of course!")
                && !event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            String tagId = idArgs.get(1);

            tagSystem.delete(tagId);

            event.getMessage().delete().queue();

            event
                .replyEmbeds(new EmbedBuilder().setColor(Color.GREEN)
                    .setTitle("Success")
                    .setTimestamp(LocalDateTime.now())
                    .setFooter(event.getUser().getAsTag())
                    .setDescription("Successfully deleted tag '" + tagId + "'")
                    .build())
                .setEphemeral(true)
                .queue();
        } else if (event.getButton().getLabel().equals("Abort")) {
            event.getMessage().delete().queue();
        }
    }
}
