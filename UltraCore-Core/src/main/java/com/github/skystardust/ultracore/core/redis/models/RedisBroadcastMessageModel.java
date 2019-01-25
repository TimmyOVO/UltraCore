package com.github.skystardust.ultracore.core.redis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RedisBroadcastMessageModel extends AbstractRedisModel {
    private String instanceName;
    private String chatContent;
    private boolean jsonFormatEnable;

    public RedisBroadcastMessageModel(String instanceName, String chatContent) {
        this.instanceName = instanceName;
        this.chatContent = chatContent;
        this.jsonFormatEnable = false;
    }
}
