package com.github.skystardust.ultracore.core.configuration;

import lombok.Builder;
import lombok.Data;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;

@Data
@Builder
public class ConfigurationClassSetter {
    private Map<String, Object> configurationData;
    private Class classToSet;
    private Object classInstance;

    public ConfigurationClassSetter(Map<String, Object> configurationData, Class classToSet, @Nullable Object classInstance) {
        this.configurationData = configurationData;
        this.classToSet = classToSet;
        this.classInstance = classInstance;
    }

    public void start() {
        configurationData.forEach((name, value) -> {
            for (Field field : classToSet.getDeclaredFields()) {
                if (field.getName().equals(name)) {
                    field.setAccessible(true);
                    try {
                        field.set(classInstance, value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
