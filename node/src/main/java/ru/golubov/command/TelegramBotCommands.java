package ru.golubov.command;

import lombok.Getter;

@Getter
public enum TelegramBotCommands {
    START_COMMAND("/start"),
    CLEAR_COMMAND("/clear");

    private final String command;

    TelegramBotCommands(String command) {
        this.command = command;
    }

}
