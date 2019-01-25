package com.github.skystardust.ultracore.bukkit.commands;

import com.github.skystardust.ultracore.bukkit.UltraCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class MainCommandSpec extends CommandSpec {
    private List<SubCommandSpec> childMainCommandSpecList;

    private MainCommandSpec(Builder builder) {
        setCommandSpecExecutor(builder.commandSpecExecutor);
        setPermission(builder.permission);
        setDescription(builder.description);
        setAliases(builder.aliases);
        setChildMainCommandSpecList(builder.childMainCommandSpecList);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public void register() {
        Objects.requireNonNull(commandSpecExecutor);
        Objects.requireNonNull(aliases);
        try {
            Constructor<PluginCommand> declaredConstructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            declaredConstructor.setAccessible(true);
            PluginCommand commandToReg = declaredConstructor.newInstance(aliases.get(0), UltraCore.getUltraCore());
            commandToReg
                    .setExecutor((commandSender, command, s, strings) -> {
                        if (permission != null) {
                            if (!commandSender.hasPermission(permission)) {
                                return false;
                            }
                        }
                        return childMainCommandSpecList.stream()
                                .filter(subCommandSpec -> strings.length > 0)
                                .filter(subCommandSpec -> subCommandSpec.getAliases().contains(strings[0]))
                                .findFirst()
                                .map(subCommandSpec -> subCommandSpec.execute(commandSender, Arrays.stream(strings).skip(1).toArray(String[]::new)))
                                .orElseGet(() -> commandSpecExecutor.executeCommand(commandSender, strings));
                    });
            commandToReg.setAliases(aliases)
                    .setDescription(description)
                    .setPermission(permission);
            Field commandMap = Bukkit.getServer().getClass()
                    .getDeclaredField("commandMap");
            commandMap.setAccessible(true);
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) commandMap.get(Bukkit.getServer());
            simpleCommandMap.register(aliases.get(0), commandToReg);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public void test(String[] args) {

    }

    public static final class Builder {
        private CommandSpecExecutor commandSpecExecutor;
        private String permission;
        private String description;
        private List<String> aliases;
        private List<SubCommandSpec> childMainCommandSpecList;

        private Builder() {
        }

        @Nonnull
        public Builder withCommandSpecExecutor(@Nonnull CommandSpecExecutor val) {
            commandSpecExecutor = val;
            return this;
        }

        @Nonnull
        public Builder withPermission(@Nullable String val) {
            permission = val;
            return this;
        }

        @Nonnull
        public Builder withDescription(@Nullable String val) {
            description = val;
            return this;
        }

        @Nonnull
        public Builder withAliases(@Nonnull List<String> val) {
            aliases = val;
            return this;
        }

        public Builder addAlias(@Nonnull String val) {
            if (aliases == null) {
                aliases = new ArrayList<>();
            }
            this.aliases.add(val);
            return this;
        }

        @Nonnull
        public Builder withChildCommandSpecList(@Nonnull List<SubCommandSpec> val) {
            childMainCommandSpecList = val;
            return this;
        }

        @Nonnull
        public Builder childCommandSpec(@Nonnull SubCommandSpec val) {
            if (childMainCommandSpecList == null) {
                childMainCommandSpecList = new ArrayList<>();
            }
            childMainCommandSpecList.add(val);
            return this;
        }

        @Nonnull
        public MainCommandSpec build() {
            if (childMainCommandSpecList == null) {
                childMainCommandSpecList = new ArrayList<>();
            }
            if (description == null) {
                description = "No description";
            }
            return new MainCommandSpec(this);
        }
    }
}
