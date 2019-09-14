package com.github.skystardust.ultracore.bukkit.modules.network;

import com.github.skystardust.ultracore.bukkit.UltraCore;
import com.github.skystardust.ultracore.bukkit.modules.network.api.IMessageForge;
import com.github.skystardust.ultracore.core.PluginInstance;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.collection.IntObjectHashMap;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class ChannelHandler {
    private String channel;
    private PluginInstance pluginInstance;
    private AtomicInteger packetIdGetter;
    private Map<Integer, Class<? extends IMessageForge>> messages;

    protected ChannelHandler(@NonNull String channel, @NonNull PluginInstance pluginInstance) {
        this.channel = channel;
        this.pluginInstance = pluginInstance;
        this.packetIdGetter = new AtomicInteger();
        this.messages = new IntObjectHashMap<>();
        UltraCore ultraCore = UltraCore.getUltraCore();
        Bukkit.getMessenger().registerIncomingPluginChannel(ultraCore, channel, (s, player, bytes) -> {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
            int packetId = byteBuf.readByte();
            Class<? extends IMessageForge> m = messages.get(packetId);
            if (m == null) {
                throw new UnsupportedOperationException(" unknown packet id " + packetId);
            }
            try {
                IMessageForge iMessageForge = m.newInstance();
                iMessageForge.read(byteBuf);
                iMessageForge.onMessage(player);
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Bukkit.getMessenger().registerOutgoingPluginChannel(ultraCore, channel);
    }

    public ChannelHandler registerMessage(Class<? extends IMessageForge> message) {
        messages.put(packetIdGetter.getAndIncrement(), message);
        return this;
    }

    public void send(IMessageForge iMessageForge, Player target) {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(getPacketIdGetter(iMessageForge.getClass()));
        iMessageForge.write(buffer);
        target.sendPluginMessage(UltraCore.getUltraCore(), channel, byteBufAsByteArray(buffer));
    }

    private byte[] byteBufAsByteArray(ByteBuf buf) {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        return bytes;
    }

    public int getPacketIdGetter(Class<? extends IMessageForge> cl) {
        return messages
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() == cl)
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(0);
    }
}
