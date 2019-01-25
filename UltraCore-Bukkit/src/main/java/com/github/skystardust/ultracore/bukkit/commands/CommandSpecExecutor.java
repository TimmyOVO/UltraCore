package com.github.skystardust.ultracore.bukkit.commands;

import org.bukkit.command.CommandSender;

public interface CommandSpecExecutor {
    boolean executeCommand(CommandSender commandSender, String[] args);
}
