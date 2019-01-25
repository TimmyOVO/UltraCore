package com.github.skystardust.ultracore.bungeecord.commands;

import com.github.skystardust.ultracore.bungeecord.UltraCore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        Command command = new Command(aliases.get(0), permission, aliases.toArray(new String[0])) {
            @Override
            public void execute(CommandSender commandSender, String[] strings) {
                if (permission != null) {
                    if (!commandSender.hasPermission(permission)) {
                        return;
                    }
                }
                childMainCommandSpecList.stream()
                        .filter(subCommandSpec -> strings.length > 0)
                        .filter(subCommandSpec -> subCommandSpec.getAliases().contains(strings[0]))
                        .findFirst()
                        .map(subCommandSpec -> subCommandSpec.execute(commandSender, Arrays.stream(strings).skip(1).toArray(String[]::new)))
                        .orElseGet(() -> commandSpecExecutor.executeCommand(commandSender, strings));
            }
        };
        ProxyServer.getInstance().getPluginManager().registerCommand(UltraCore.getUltraCore(), command);
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
            return new MainCommandSpec(this);
        }
    }
}
