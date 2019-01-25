package com.github.skystardust.ultracore.core.redis.models;

import com.github.skystardust.ultracore.core.utils.FileUtils;

public abstract class AbstractRedisModel {

    public static <T> T valueOf(String json, Class<? extends T> typeOf) {
        return FileUtils.GSON.fromJson(json, typeOf);
    }

    public String asString() {
        return FileUtils.GSON.toJson(this);
    }
}
