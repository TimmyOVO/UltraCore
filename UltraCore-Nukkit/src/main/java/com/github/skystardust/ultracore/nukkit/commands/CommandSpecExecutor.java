package com.github.skystardust.ultracore.nukkit.commands;


import cn.nukkit.command.CommandSender;

public interface CommandSpecExecutor {
    boolean executeCommand(CommandSender commandSender, String[] args);
}
