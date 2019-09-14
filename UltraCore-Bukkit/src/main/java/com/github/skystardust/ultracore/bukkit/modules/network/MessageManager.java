package com.github.skystardust.ultracore.bukkit.modules.network;

import com.github.skystardust.ultracore.core.PluginInstance;
import com.google.common.collect.Lists;
import lombok.NonNull;

import java.util.List;

public class MessageManager {
    private static MessageManager instance = new MessageManager();
    private List<ChannelHandler> handlers = Lists.newArrayList();

    private MessageManager() {

    }

    public static MessageManager getInstance() {
        return instance;
    }

    public ChannelHandler enableIMessageFor(@NonNull PluginInstance pluginInstance, @NonNull String channel) {
        ChannelHandler channelHandler = new ChannelHandler(channel, pluginInstance);
        handlers.add(channelHandler);
        return channelHandler;
    }

}
