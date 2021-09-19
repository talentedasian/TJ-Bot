package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.togetherjava.tjbot.commands.CommandUtility;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;

public final class TagUtility {
    private TagUtility() {}

    public static MessageEmbed generateEmbed(String tag, String requestor) {
        return new EmbedBuilder().setDescription(tag)
            .setTimestamp(LocalDateTime.now())
            .setFooter(requestor)
            .setColor(new Color(tag.hashCode()))
            .build();
    }

    public static void sendTag(MessageChannel channel, String tagId, String requestor,
            TagSystem tagSystem, boolean isRaw, String componentId) {
        String content = tagSystem.get(tagId);

        channel
            .sendMessageEmbeds(TagUtility
                .generateEmbed(isRaw ? CommandUtility.escape(content) : content, requestor))
            .setActionRow(Button.of(ButtonStyle.DANGER, componentId, "Delete",
                    Emoji.fromUnicode("\uD83D\uDDD1")))
            .queue();
    }

    public static void replyTag(SlashCommandEvent event, String tagId, String requestor,
            TagSystem tagSystem, boolean isRaw, String componentId) {
        if (tagSystem.exists(tagId)) {
            String content = tagSystem.get(tagId);

            event
                .replyEmbeds(TagUtility
                    .generateEmbed(isRaw ? CommandUtility.escape(content) : content, requestor))
                .addActionRow(Button.of(ButtonStyle.DANGER, componentId, "Delete",
                        Emoji.fromUnicode("\uD83D\uDDD1")))
                .queue();
        } else {
            event
                .replyEmbeds(buildAllTagsEmbed(requestor, tagSystem)
                    .setTitle("Could not find tag '" + tagId + "'")
                    .build())
                .setEphemeral(true)
                .queue();
        }
    }

    public static EmbedBuilder buildAllTagsEmbed(String user, TagSystem tagSystem) {
        return new EmbedBuilder().setColor(Color.MAGENTA)
            .setTimestamp(LocalDateTime.now())
            .setFooter(user)
            .setDescription(String.join(", ", tagSystem.retrieveIds()));
    }
}
