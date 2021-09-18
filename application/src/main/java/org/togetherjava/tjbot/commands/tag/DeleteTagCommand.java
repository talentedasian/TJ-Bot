package org.togetherjava.tjbot.commands.tag;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import org.togetherjava.tjbot.commands.AbstractCommand;
import org.togetherjava.tjbot.tag.TagSystem;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

public class DeleteTagCommand extends AbstractCommand {
    private final TagSystem tagSystem;

    public DeleteTagCommand(TagSystem tagSystem) {
        super("deletetag", "Create a tag", true);

        this.tagSystem = tagSystem;
    }

    @Override
    public CommandData addOptions(CommandData data) {
        return data.addOption(OptionType.STRING, "id", "Tag id", true);
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Member member = event.getMember();

        if (member.hasPermission(Permission.MESSAGE_MANAGE)) {
            String tagId = event.getOption("id").getAsString();

            if (!tagSystem.exists(tagId)) {
                event.reply("This tag does not exist").setEphemeral(true).queue();

                return;
            }

            event.reply("You sure? Confirming this will delete the tag '" + tagId + "'")
                .addActionRow(
                        Button.of(ButtonStyle.SUCCESS,
                                generateComponentId(member.getId(), tagId, "1"), "Of course!"),
                        Button.of(ButtonStyle.DANGER, generateComponentId(member.getId(), "2"),
                                "Abort"))
                .queue();
        } else {
            event.reply("You need the MESSAGE_MANAGE permission to use this command!")
                .setEphemeral(true)
                .queue();
        }
    }

    @Override
    public void onButtonClick(ButtonClickEvent event, List<String> idArgs) {
        Member member = event.getMember();
        String tagId = idArgs.get(1);

        if (member.getId().equals(idArgs.get(0))) {
            if (event.getButton().getStyle() == ButtonStyle.SUCCESS) {
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
            } else {
                event.getMessage().delete().queue();
            }
        }
    }
}
