package org.togetherjava.tjbot.commands;

import org.jetbrains.annotations.NotNull;

/**
 * Makes usage of constructor to store the {@link #commandName}, {@link #description} and the
 * {@link #isGuildOnly} <br>
 * This helps against a tiny bit of duplicated code.
 * @see Command
 */
public class AbstractCommand implements Command {
    private final String commandName;
    private final String description;
    private final boolean isGuildOnly;

    /**
     * Check the linked docs for their requirements and more
     * @param commandName {@link Command#getCommandName()}
     * @param description {@link Command#getDescription()}
     * @param isGuildOnly {@link Command#isGuildOnly()}
     *
     * @see Command
     */
    public AbstractCommand(String commandName, String description, boolean isGuildOnly) {
        this.commandName = commandName;
        this.description = description;
        this.isGuildOnly = isGuildOnly;
    }

    public AbstractCommand(String commandName, String description) {
        this(commandName, description, false);
    }

    @Override
    public @NotNull String getCommandName() {
        return commandName;
    }

    @Override
    public @NotNull String getDescription() {
        return description;
    }

    @Override
    public boolean isGuildOnly() {
        return isGuildOnly;
    }
}
