package com.github.skystardust.ultracore.core.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RedisConfiguration {
    private String redisUrl;
}
