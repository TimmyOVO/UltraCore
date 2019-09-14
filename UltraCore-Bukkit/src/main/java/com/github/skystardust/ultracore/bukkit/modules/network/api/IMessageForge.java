package com.github.skystardust.ultracore.bukkit.modules.network.api;

import io.netty.buffer.ByteBuf;
import org.bukkit.entity.Player;

public interface IMessageForge {
    Side getTo();

    void read(ByteBuf buf);

    void write(ByteBuf buf);

    void onMessage(Player player);
}
