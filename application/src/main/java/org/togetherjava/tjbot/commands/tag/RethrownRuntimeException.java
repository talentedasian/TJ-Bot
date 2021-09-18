package org.togetherjava.tjbot.commands.tag;

public class RethrownRuntimeException extends RuntimeException {
    public RethrownRuntimeException(Throwable throwable) {
        super(throwable);
    }
}
