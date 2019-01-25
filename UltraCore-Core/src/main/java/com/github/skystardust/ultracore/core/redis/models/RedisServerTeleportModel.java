package com.github.skystardust.ultracore.core.redis.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RedisServerTeleportModel extends AbstractRedisModel {
    private String serverName;
    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public RedisServerTeleportModel(String serverName, String world) {
        this.serverName = serverName;
        this.world = world;
    }
}
