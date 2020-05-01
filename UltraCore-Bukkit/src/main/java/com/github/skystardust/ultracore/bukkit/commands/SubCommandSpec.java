package com.github.skystardust.ultracore.bukkit.commands;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.CommandSender;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class SubCommandSpec extends CommandSpec {
    private List<SubCommandSpec> subCommandSpecList;

    private SubCommandSpec(Builder builder) {
        setCommandSpecExecutor(builder.commandSpecExecutor);
        setPermission(builder.permission);
        setDescription(builder.description);
        setAliases(builder.aliases);
        setSubCommandSpecList(builder.subCommandSpecList);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public boolean execute(CommandSender commandSender, String[] args) {
        if (permission != null) {
            if (!commandSender.hasPermission(permission)) {
                return false;
            }
        }
        return subCommandSpecList.stream()
                .filter(subCommandSpec -> args.length > 0)
                .filter(subCommandSpec -> subCommandSpec.getAliases().contains(args[0]))
                .findFirst()
                .map(subCommandSpec -> subCommandSpec.execute(commandSender, Arrays.stream(args)
                        .skip(1)
                        .toArray(String[]::new)
                ))
                .orElseGet(() -> commandSpecExecutor.executeCommand(commandSender, args));
    }

    @Override
    public void register() {
        throw new UnsupportedOperationException();
    }

    public static final class Builder {
        private CommandSpecExecutor commandSpecExecutor;
        private String permission;
        private String description;
        private List<String> aliases;
        private List<SubCommandSpec> subCommandSpecList;

        private Builder() {
        }

        @Nonnull
        public Builder withCommandSpecExecutor(@Nonnull CommandSpecExecutor val) {
            commandSpecExecutor = val;
            return this;
        }

        @Nonnull
        public Builder withPermission(@Nonnull String val) {
            permission = val;
            return this;
        }

        @Nonnull
        public Builder withDescription(@Nonnull String val) {
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
        public Builder childCommandSpec(@Nonnull SubCommandSpec val) {
            if (subCommandSpecList == null) {
                subCommandSpecList = new ArrayList<>();
            }
            subCommandSpecList.add(val);
            return this;
        }


        @Nonnull
        public Builder withSubCommandSpecList(@Nonnull List<SubCommandSpec> val) {
            subCommandSpecList = val;
            return this;
        }

        @Nonnull
        public SubCommandSpec build() {
            if (subCommandSpecList == null) {
                subCommandSpecList = new ArrayList<>();
            }
            return new SubCommandSpec(this);
        }
    }
}
