package com.github.skystardust.ultracore.bukkit.commands;

import com.github.skystardust.ultracore.bukkit.UltraCore;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

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
        setTabSpecExecutor(builder.commandSpecTabExecutor);
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
            commandToReg.setTabCompleter((commandSender, command, s, args) -> {
                List<SubCommandSpec> parentCommandSpec = null;
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    if (parentCommandSpec == null){
                        List<SubCommandSpec> allMatchedSubCommands = this.childMainCommandSpecList.stream()
                                .filter(subCommandSpec -> subCommandSpec.getAliases().contains(arg))
                                .collect(Collectors.toList());
                        if (!allMatchedSubCommands.isEmpty()){
                            parentCommandSpec = allMatchedSubCommands;
                        }else {
                            List<String> retArgs=null;
                            if(this.tabSpecExecutor!=null){
                                retArgs = tabSpecExecutor.executeTab(commandSender,args);
                            }
                            if(retArgs==null){
                                return this.childMainCommandSpecList.stream()
                                        .flatMap(subCommandSpec -> subCommandSpec.getAliases().stream())
                                        .filter(s1 -> s1.contains(arg))
                                        .collect(Collectors.toList());
                            }else{
                                return retArgs;
                            }
                        }
                    }else {
                        List<SubCommandSpec> allMatchedSubCommands = parentCommandSpec.stream()
                                .filter(subCommandSpec -> subCommandSpec.getSubCommandSpecList().stream().anyMatch(subCommandSpec1 -> subCommandSpec1.getAliases().contains(arg)))
                                .flatMap(subCommandSpec -> subCommandSpec.getSubCommandSpecList().stream())
                                .collect(Collectors.toList());
                        if (!allMatchedSubCommands.isEmpty()){
                            parentCommandSpec = allMatchedSubCommands;
                        }else {
                            int finalI = i;
                            List<String> retArgs=null;
                            if(this.tabSpecExecutor!=null){
                                retArgs = tabSpecExecutor.executeTab(commandSender,Arrays.stream(args).skip(finalI).toArray(String[]::new));
                            }
                            if(retArgs==null) {
                                temp.removeIf(subCommandSpec -> !subCommandSpec.getAliases().contains(args[finalI - 1]));
                                return temp.stream()
                                        .flatMap(subCommandSpec -> subCommandSpec.getSubCommandSpecList().stream().flatMap(subCommandSpec1 -> subCommandSpec1.getAliases().stream()))
                                        .filter(s1 -> s1.contains(arg))
                                        .collect(Collectors.toList());
                            }else{
                                return retArgs;
                            }
                        }
                    }
                }
                if (parentCommandSpec != null){
                    return parentCommandSpec.stream()
                            .flatMap(subCommandSpec -> subCommandSpec.getAliases().stream())
                            .collect(Collectors.toList());
                }
                return Collections.emptyList();

            });

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
        private CommandSpecTabExecutor commandSpecTabExecutor;
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
        public Builder withCommandSpecTabExecutor(@Nonnull CommandSpecTabExecutor val) {
            commandSpecTabExecutor = val;
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

    @Getter
    @Setter
    @AllArgsConstructor
    @lombok.Builder
    public static final class TabPair<T, V> {
        private T key;
        public V value;
    }
}
