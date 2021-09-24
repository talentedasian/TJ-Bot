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

/**
 * Utility class for the command system.<br>
 * Available methods:<br>
 * - {@link #generateEmbed(String, String)}<br>
 * - {@link #sendTag(MessageChannel, String, String, TagSystem, boolean, String)}<br>
 * - {@link #replyTag(SlashCommandEvent, String, String, TagSystem, boolean, String)}<br>
 * - {@link #buildAllTagsEmbed(String, TagSystem)}
 *
 * @author illuminator3
 */
public final class TagUtility {
    private TagUtility() {}

    /**
     * Generates an embed with the given content.
     *
     * @param content content
     * @param requestor user that requested the embed
     * @return the generated embed
     * @author illuminator3
     */
    public static MessageEmbed generateEmbed(String content, String requestor) {
        return new EmbedBuilder().setDescription(content)
            .setTimestamp(LocalDateTime.now())
            .setFooter(requestor)
            .setColor(new Color(content.hashCode()))
            .build();
    }

    /**
     * Sends a tag into a given channel.
     *
     * @param channel channel the tag was requested in
     * @param tagId tag id
     * @param requestor user that requested the tag
     * @param tagSystem current tag system instance
     * @param isRaw if the tag should be displayed raw
     * @param componentId generated componentId based on the user id
     * @author illuminator3
     */
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

    /**
     * Replies to a message with a given tag.
     *
     * @param event slash command event causing this tag request
     * @param tagId tag id
     * @param requestor user that requested the tag
     * @param tagSystem current tag system instance
     * @param isRaw if the tag should be displayed raw
     * @param componentId generated componentId based on the user id
     * @author illuminator3
     */
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

    /**
     * Builds an embed with all available tag ids as its description
     *
     * @param user user that requested the embed
     * @param tagSystem current tag system instance
     * @return the generated embed
     * @author illuminator3
     */
    public static EmbedBuilder buildAllTagsEmbed(String user, TagSystem tagSystem) {
        return new EmbedBuilder().setColor(Color.MAGENTA)
            .setTimestamp(LocalDateTime.now())
            .setFooter(user)
            .setDescription(String.join(", ", tagSystem.retrieveIds()));
    }
}
