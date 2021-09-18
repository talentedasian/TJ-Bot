package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class RawTagCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public RawTagCommand(TagSystem tagSystem) {
        super("rawtag", "View a tag in its raw form", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public CommandData addOptions(CommandData data) {
        return data.addOption(OptionType.STRING, "id", "Tag id");
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        OptionMapping idOption = event.getOption("id");

        if (idOption != null) {
            String tagId = idOption.getAsString();

            replyTag(event, tagId, event.getUser().getAsTag(), event.getUser().getId());
        } else {
            SelectionMenu.Builder menu =
                    SelectionMenu.create(generateComponentId(event.getUser().getId(), "1"))
                        .setRequiredRange(1, 1);

            for (String tag : tagSystem.retrieveIds()) {
                menu.addOption(tag, tag);
            }

            event.reply("Choose a tag")
                .addActionRow(menu.build())
                .addActionRow(Button.of(ButtonStyle.DANGER,
                        generateComponentId(event.getUser().getId(), "2"), "Delete",
                        Emoji.fromUnicode("\uD83D\uDDD1")))
                .queue();
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event, List<String> idArgs) {
        String userId = idArgs.get(0);

        if (event.getUser().getId().equals(userId)
                || event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getMessage().delete().queue();
        } else {
            event.reply(":police_car: Button theft is illegal").setEphemeral(true).queue();
        }
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event, List<String> idArgs) {
        String userId = idArgs.get(0);

        if (event.getUser().getId().equals(userId)) {
            SelectOption option = event.getSelectedOptions().get(0);
            String tagId = option.getLabel();

            event.getMessage().delete().queue();

            sendTag(event.getMessageChannel(), tagId, event.getUser().getAsTag(), userId);
        } else {
            event.reply(":police_car: Selection menu theft is illegal").setEphemeral(true).queue();
        }
    }

    private void sendTag(MessageChannel channel, String tagId, String requestor, String userId) {
        channel.sendMessageEmbeds(generateEmbed(escape(tagSystem.get(tagId)), requestor))
            .setActionRow(Button.of(ButtonStyle.DANGER, generateComponentId(userId), "Delete",
                    Emoji.fromUnicode("\uD83D\uDDD1")))
            .queue();
    }

    private void replyTag(SlashCommandEvent event, String tagId, String requestor, String userId) {
        if (tagSystem.exists(tagId)) {
            event.replyEmbeds(generateEmbed(escape(tagSystem.get(tagId)), requestor))
                .addActionRow(Button.of(ButtonStyle.DANGER, generateComponentId(userId), "Delete",
                        Emoji.fromUnicode("\uD83D\uDDD1")))
                .queue();
        } else {
            EmbedBuilder builder = new EmbedBuilder().setColor(Color.RED)
                .setTimestamp(LocalDateTime.now())
                .setFooter(requestor)
                .setTitle("Could not find tag '" + tagId + "'")
                .setDescription("All available tagSystem");

            for (Map.Entry<String, String> entry : tagSystem.retrieve().entrySet()) {
                String id = entry.getKey(), text = entry.getValue(),
                        preview = text.substring(0, Math.min(text.length(), 50));

                builder.addField(id, preview + "...", true);
            }

            event.replyEmbeds(builder.build()).setEphemeral(true).queue();
        }
    }

    private MessageEmbed generateEmbed(String tag, String requestor) {
        return new EmbedBuilder().setDescription(tag)
            .setTimestamp(LocalDateTime.now())
            .setFooter(requestor)
            .setColor(new Color(tag.hashCode()))
            .build();
    }

    private String escape(String s) {
        return s.replaceAll("([^a-zA-Z0-9 \n\r])", "\\\\$1");
    }
}
