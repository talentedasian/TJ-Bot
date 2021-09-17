package org.togetherjava.tjbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The command handler
 *
 * Commands need to be added to the commandList
 */
public class CommandHandler extends ListenerAdapter {
    private final static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final List<Command> commandList = new ArrayList<>();
    private final Map<String, Command> commandMap;

    public CommandHandler() {
        commandList.addAll(List.of(new ReloadCommand(this)));

        commandMap = commandList.stream()
            .collect(Collectors.toMap(Command::getCommandName, Function.identity()));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        event.getJDA().getGuildCache().forEach(guild -> {
            guild.retrieveCommands().queue(commands -> {
                boolean hasReloadCommand =
                        commands.stream().anyMatch(command -> command.getName().equals("reload"));

                if (!hasReloadCommand) {
                    Command command = commandMap.get("reload");
                    guild.upsertCommand(command.addOptions(
                            new CommandData(command.getCommandName(), command.getDescription())))
                        .queue();
                }
            }, throwable -> {
                handleErrors(throwable, guild);
            });
        });
    }

    private static void handleErrors(Throwable throwable, Guild guild) {
        new ErrorHandler().handle(ErrorResponse.MISSING_ACCESS, errorResponseException -> {
            // * Hard to read, there's no other accurate way to get a channel you've the write
            // * permission in.
            guild.getTextChannelCache().stream().filter(textChannel -> {
                return guild.getPublicRole().hasPermission(textChannel, Permission.MESSAGE_WRITE);
            }).findFirst().ifPresent(textChannel -> {
                textChannel
                    .sendMessage("This bot needs the commands scope, please invite it correctly!\n"
                            + "You can join https://discord.gg/UJBwS2Hvcx for more info, I'll leave now.")
                    .queue();
                guild.leave().queue();
            });
            logger.error("Guild '{}' doesn't have the required commands scope!", guild.getName(),
                    throwable);
        }).accept(throwable);
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        Command command = commandMap.get(event.getName());

        if (command != null) {
            executorService.submit(() -> command.onSlashCommand(event));
        } else {
            logger.error("Command '{}' doesn't exist?", event.getName());
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        getArgumentsAndCommandByEvent(event, (command, args) -> command.onButtonClick(event, args));
    }

    @Override
    public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
        getArgumentsAndCommandByEvent(event,
                (command, args) -> command.onSelectionMenu(event, args));
    }

    /**
     * Gets the arguments as a {@link List} and gets the {@link Command} by the event.
     *
     * @param event a {@link GenericComponentInteractionCreateEvent}
     * @param onSucceed a {@link BiConsumer} that takes the command and the arguments
     */
    private void getArgumentsAndCommandByEvent(GenericComponentInteractionCreateEvent event,
            BiConsumer<Command, List<String>> onSucceed) {
        String[] argsArray = event.getComponentId().split("-");
        Command command = commandMap.get(argsArray[0]);

        if (command != null) {
            List<String> fullList = new ArrayList<>(Arrays.asList(argsArray));

            final List<String> listExcludingStart = fullList.subList(2, fullList.size());

            executorService.submit(() -> onSucceed.accept(command, listExcludingStart));
        }
    }

    public List<Command> getCommandList() {
        return commandList;
    }
}
