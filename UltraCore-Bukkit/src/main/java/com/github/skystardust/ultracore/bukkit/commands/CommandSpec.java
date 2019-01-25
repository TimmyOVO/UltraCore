package com.github.skystardust.ultracore.bukkit.commands;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class CommandSpec {
    protected CommandSpecExecutor commandSpecExecutor;
    protected String permission;
    protected String description;
    protected List<String> aliases;

    public abstract void register();
}
