package org.togetherjava.tjbot.commands;

public final class CommandUtility {
    public static String escape(String s) {
        return s.replaceAll("([^a-zA-Z0-9 \n\r])", "\\\\$1");
    }
}
