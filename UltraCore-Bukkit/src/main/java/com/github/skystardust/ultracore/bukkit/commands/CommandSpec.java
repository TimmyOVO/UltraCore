package com.github.skystardust.ultracore.bukkit.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public abstract class CommandSpec {
    protected CommandSpecExecutor commandSpecExecutor;
    protected CommandSpecTabExecutor tabSpecExecutor;
    protected String permission;
    protected String description;
    protected List<String> aliases;

    public abstract void register();
}
