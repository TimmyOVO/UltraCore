package com.github.skystardust.ultracore.core.redis;

import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.configuration.RedisConfiguration;
import com.github.skystardust.ultracore.core.redis.listeners.DefaultChannelListener;
import com.github.skystardust.ultracore.core.redis.models.AbstractRedisModel;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.google.common.collect.Maps;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisManager {
    @Setter(AccessLevel.NONE)
    private Class<? extends PluginInstance> ownerClass;
    @Setter(AccessLevel.NONE)
    private PluginInstance ownerInstance;
    private RedisConfiguration redisConfiguration;
    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> redisConnection;
    private RedisCommands<String, String> redisCommands;
    private StatefulRedisPubSubConnection<String, String> redisMessageQueueCommands;
    private Map<String, RedisPubSubListener<String, String>> channelMessageListeners;

    private RedisManager(Builder builder) {
        ownerClass = builder.ownerClass;
        ownerInstance = builder.ownerInstance;
        setRedisConfiguration(builder.redisConfiguration);
        setRedisClient(builder.redisClient);
        setRedisConnection(builder.redisConnection);
        setRedisCommands(builder.redisCommands);
        setRedisMessageQueueCommands(builder.redisMessageQueueCommands);
        setChannelMessageListeners(builder.channelMessageListeners);
    }

    public static Builder createInstance(Class<? extends PluginInstance> ownerClass, PluginInstance ownerInstance) {
        return RedisManager.newBuilder()
                .withOwnerClass(ownerClass)
                .withOwnerInstance(ownerInstance)
                .withRedisConfiguration(RedisConfiguration.builder()
                        .redisUrl("redis://localhost")
                        .build());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(@Nonnull RedisManager copy) {
        Builder builder = new Builder();
        builder.ownerClass = copy.getOwnerClass();
        builder.ownerInstance = copy.getOwnerInstance();
        builder.redisConfiguration = copy.getRedisConfiguration();
        builder.redisClient = copy.getRedisClient();
        builder.redisConnection = copy.getRedisConnection();
        builder.redisCommands = copy.getRedisCommands();
        builder.redisMessageQueueCommands = copy.getRedisMessageQueueCommands();
        builder.channelMessageListeners = copy.getChannelMessageListeners();
        return builder;
    }

    public Optional<RedisManager> openConnection() {
        Objects.requireNonNull(ownerClass, "Unable find ownerClass");
        Objects.requireNonNull(ownerInstance, "Unable find ownerInstance");
        Objects.requireNonNull(redisConfiguration, "Unable find redisConfiguration");
        ownerInstance.getPluginLogger().info("开始初始化 [" + ownerInstance.getName() + "] 的 Redis 管理器!");
        if (!ownerInstance.getDataFolder().isDirectory() || !ownerInstance.getDataFolder().exists()) {
            if (!ownerInstance.getDataFolder().mkdirs()) {
                ownerInstance.getPluginLogger().warning("无法创建插件配置文件夹!");
                return Optional.empty();
            }
        }
        File redisConfigurationFile = new File(ownerInstance.getDataFolder(), "redis.conf");
        if (!redisConfigurationFile.exists()) {
            ownerInstance.getPluginLogger().info("无法找到 Redis 配置文件,正在创建!");
            FileUtils.writeFileContent(redisConfigurationFile, FileUtils.GSON.toJson(redisConfiguration));
            ownerInstance.getPluginLogger().info("创建 Redis 配置文件模板完成!");
        }
        ownerInstance.getPluginLogger().info("正在读取 Redis 配置文件");
        this.redisConfiguration = FileUtils.GSON.fromJson(FileUtils.readFileContent(redisConfigurationFile), RedisConfiguration.class);
        ownerInstance.getPluginLogger().info("Redis 配置文件读取成功!");
        ownerInstance.getPluginLogger().info("正在尝试连接到 Redis ....");
        try {
            this.redisClient = RedisClient.create(redisConfiguration.getRedisUrl());
            this.redisConnection = redisClient.connect();
            this.redisConnection.setTimeout(Duration.ofSeconds(30));
            this.redisCommands = redisConnection.sync();
            this.redisMessageQueueCommands = redisClient.connectPubSub();
            this.channelMessageListeners.forEach((key, value) -> {
                redisMessageQueueCommands.addListener(value);
                redisMessageQueueCommands.sync().subscribe(key);
            });
            ownerInstance.getPluginLogger().info("连接到 [" + ownerInstance.getName() + "] 的 Redis 已成功!");
        } catch (Exception e) {
            ownerInstance.getPluginLogger().warning("连接到 Redis 失败!");
            ownerInstance.getPluginLogger().warning(e.getMessage());
            return Optional.empty();
        }
        List<Field> successResult = Arrays.stream(ownerClass.getDeclaredFields())
                .filter(field -> Arrays.stream(field.getDeclaredAnnotations()).anyMatch(annotation -> annotation.annotationType().equals(RedisManagerInject.class)))
                .filter(field -> {
                    if (!field.getType().equals(RedisManager.class)) {
                        return false;
                    }
                    field.setAccessible(true);
                    try {
                        field.set(ownerInstance, this);
                        ownerInstance.getPluginLogger().info("已注入 [" + ownerClass.getName() + "]-[" + field.getName() + "] !");
                        return true;
                    } catch (IllegalAccessException e) {
                        ownerInstance.getPluginLogger().warning("无法设置 [" + field.getName() + " ! 已自动跳过该字段!");
                        ownerInstance.getPluginLogger().warning(e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
        if (successResult.isEmpty()) {
            ownerInstance.getPluginLogger().warning("自动注入 [" + ownerInstance.getName() + "] 未能成功注入任何 Redis 管理器!");
        }
        return Optional.of(this);
    }

    public void publishMessage(String channel, String message) {
        this.redisClient.connect().sync().publish(channel, message);
    }

    public void publishMessage(String channel, AbstractRedisModel model) {
        publishMessage(channel, model.asString());
    }

    public static final class Builder {
        private Class<? extends PluginInstance> ownerClass;
        private PluginInstance ownerInstance;
        private RedisConfiguration redisConfiguration;
        private RedisClient redisClient;
        private StatefulRedisConnection<String, String> redisConnection;
        private RedisCommands<String, String> redisCommands;
        private StatefulRedisPubSubConnection<String, String> redisMessageQueueCommands;
        private Map<String, RedisPubSubListener<String, String>> channelMessageListeners;

        private Builder() {
        }

        @Nonnull
        public Builder withOwnerClass(@Nonnull Class<? extends PluginInstance> val) {
            ownerClass = val;
            return this;
        }

        @Nonnull
        public Builder withOwnerInstance(@Nonnull PluginInstance val) {
            ownerInstance = val;
            return this;
        }

        @Nonnull
        public Builder withRedisConfiguration(@Nonnull RedisConfiguration val) {
            redisConfiguration = val;
            return this;
        }

        @Nonnull
        public Builder withRedisClient(@Nonnull RedisClient val) {
            redisClient = val;
            return this;
        }

        @Nonnull
        public Builder withRedisConnection(@Nonnull StatefulRedisConnection<String, String> val) {
            redisConnection = val;
            return this;
        }

        @Nonnull
        public Builder withRedisCommands(@Nonnull RedisCommands<String, String> val) {
            redisCommands = val;
            return this;
        }

        @Nonnull
        public Builder withRedisMessageQueueCommands(@Nonnull StatefulRedisPubSubConnection<String, String> val) {
            redisMessageQueueCommands = val;
            return this;
        }

        @Nonnull
        public Builder withChannelMessageListeners(@Nonnull Map<String, RedisPubSubListener<String, String>> val) {
            channelMessageListeners = val;
            return this;
        }

        public Builder addRedisMessageListener(String channel, @Nonnull RedisPubSubListener<String, String> listener) {
            if (channelMessageListeners == null) {
                channelMessageListeners = Maps.newHashMap();
            }
            channelMessageListeners.put(channel, listener);
            return this;
        }

        public Builder addRedisMessageListener(@Nonnull DefaultChannelListener listener) {
            if (channelMessageListeners == null) {
                channelMessageListeners = Maps.newHashMap();
            }
            channelMessageListeners.put(listener.getChannel(), listener);
            return this;
        }

        @Nonnull
        public RedisManager build() {
            if (channelMessageListeners == null) {
                channelMessageListeners = Maps.newHashMap();
            }
            return new RedisManager(this);
        }
    }
}
