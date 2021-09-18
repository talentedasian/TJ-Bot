package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.Map;

public class TagUtility {
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
            .sendMessageEmbeds(
                    TagUtility.generateEmbed(isRaw ? escape(content) : content, requestor))
            .setActionRow(net.dv8tion.jda.api.interactions.components.Button.of(ButtonStyle.DANGER,
                    componentId, "Delete", Emoji.fromUnicode("\uD83D\uDDD1")))
            .queue();
    }

    public static void replyTag(SlashCommandEvent event, String tagId, String requestor,
            TagSystem tagSystem, boolean isRaw, String componentId) {
        if (tagSystem.exists(tagId)) {
            String content = tagSystem.get(tagId);

            event
                .replyEmbeds(TagUtility.generateEmbed(isRaw ? escape(content) : content, requestor))
                .addActionRow(Button.of(ButtonStyle.DANGER, componentId, "Delete",
                        Emoji.fromUnicode("\uD83D\uDDD1")))
                .queue();
        } else {
            event
                .replyEmbeds(buildAllTagsEmbed(requestor, tagSystem)
                    .setTitle("Could not find tag'" + tagId + "'")
                    .setDescription("All available tags")
                    .build())
                .setEphemeral(true)
                .queue();
        }
    }

    public static String escape(String s) {
        return s.replaceAll("([^a-zA-Z0-9 \n\r])", "\\\\$1");
    }

    public static EmbedBuilder buildAllTagsEmbed(String user, TagSystem tagSystem) {
        EmbedBuilder builder = new EmbedBuilder().setColor(Color.MAGENTA)
            .setTimestamp(LocalDateTime.now())
            .setFooter(user)
            .setTitle("All available tags");

        for (Map.Entry<String, String> entry : tagSystem.retrieve().entrySet()) {
            String id = entry.getKey(), text = entry.getValue(),
                    preview = text.substring(0, Math.min(text.length(), 50));

            builder.addField(id, preview + (text.length() > 50 ? "..." : ""), true);
        }

        return builder;
    }
}
