package com.github.skystardust.ultracore.core.redis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisChatMessageModel extends AbstractRedisModel {
    private UUID targetPlayer;
    private String chatContent;
    private boolean jsonFormatEnable;

    public RedisChatMessageModel(UUID targetPlayer, String chatContent) {
        this.targetPlayer = targetPlayer;
        this.chatContent = chatContent;
        this.jsonFormatEnable = false;
    }
}
