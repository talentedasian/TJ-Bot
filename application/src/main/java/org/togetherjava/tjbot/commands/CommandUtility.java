package org.togetherjava.tjbot.commands;

/**
 * Utility class for the command system

 * @author illuminator3
 */
public final class CommandUtility {
    private CommandUtility() {}

    /**
     * Escapes every character in a string that is not a-zA-Z0-9, a space, or \n respectively \r with a backslash (\)<br><br>
     * Example:<br>
     * {@code `System.out.println("Hello World")`} would get turned into {@code \`System\.out\.println\(\"Hello World\"\)\`}
     *
     * @param s string to escape
     * @return escaped string
     * @author illuminator3
     */
    public static String escape(String s) {
        return s.replaceAll("([^a-zA-Z0-9 \n\r])", "\\\\$1");
    }
}
