package com.github.skystardust.ultracore.bukkit.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandSpecTabExecutor {
    List<String> executeTab(CommandSender commandSender, String[] args);
}
