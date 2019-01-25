package com.github.skystardust.ultracore.bungeecord.commands;

import net.md_5.bungee.api.CommandSender;

public interface CommandSpecExecutor {
    boolean executeCommand(CommandSender commandSender, String[] args);
}
