package com.github.skystardust.ultracore.core.configuration;

import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import lombok.Data;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Data
public class ConfigurationManager {
    public static final Map<PluginInstance, ConfigurationManager> PLUGIN_INSTANCE_CONFIGURATION_MANAGER_MAP = new HashMap<>();
    private PluginInstance ownPlugin;
    private Map<String, Object> configurationModels;
    private Map<String, Object> data;
    private Map.Entry<Class<?>, Object> classSetterInfo;

    public ConfigurationManager(PluginInstance ownPlugin) {
        this.ownPlugin = ownPlugin;
        this.configurationModels = new HashMap<>();
        this.data = new HashMap<>();
        PLUGIN_INSTANCE_CONFIGURATION_MANAGER_MAP.put(ownPlugin, this);
    }

    public ConfigurationManager registerConfiguration(String name, Supplier o) {
        this.configurationModels.put(name, o.get());
        return this;
    }

    public ConfigurationClassSetter init(Class clazz, @Nullable Object o) {
        if (!ownPlugin.getDataFolder().exists()) {
            ownPlugin.getDataFolder().mkdirs();
        }
        this.classSetterInfo = new Map.Entry<Class<?>, Object>() {
            @Override
            public Class<?> getKey() {
                return clazz;
            }

            @Override
            public Object getValue() {
                return o;
            }

            @Override
            public Object setValue(Object value) {
                return null;
            }
        };
        ownPlugin.getPluginLogger().info("正在初始化 " + ownPlugin.getName() + " 的配置文件!");
        configurationModels.forEach(this::loadConfiguration);
        ownPlugin.getPluginLogger().info("初始化 " + ownPlugin.getName() + " 已全部成功!");
        return ConfigurationClassSetter.builder()
                .classToSet(clazz)
                .classInstance(o)
                .configurationData(data).build();
    }

    public void loadConfiguration(String fileName, Object defaultValue) {
        loadConfiguration(new File(getOwnPlugin().getDataFolder(), fileName + ".yml"), defaultValue);
    }

    public void loadConfiguration(File file, Object defaultValue) {
        getOwnPlugin().getPluginLogger().info("初始化配置文件 " + file.getName() + " 中,请稍候..!");
        if (!file.exists()) {
            getOwnPlugin().getPluginLogger().info("正在创建配置文件 " + file.getName() + " 的模板.");
            writeConfigurationFile(file, FileUtils.GSON.toJson(defaultValue));
            getOwnPlugin().getPluginLogger().info("创建 " + file.getName() + " 的模板完成!");
        }
        getOwnPlugin().getPluginLogger().info("正在读取配置文件 " + file.getName() + " 的现有存档.");
        getData().put(file.getName(), FileUtils.GSON.fromJson(readConfigurationFile(file), defaultValue.getClass()));
        getOwnPlugin().getPluginLogger().info("读取配置文件 " + file.getName() + " 已成功.");
    }

    public void writeConfigurationFile(File file, String content) {
        FileUtils.writeFileContent(file, content);
    }

    public String readConfigurationFile(File file) {
        return FileUtils.readFileContent(file);
    }


    public void reloadFiles() throws ConfigurationException {
        try {
            init(classSetterInfo.getKey(), classSetterInfo.getValue()).start();
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    public boolean hasConfigurationFile(String name) {
        return configurationModels.get(name) != null;
    }

    public void saveFile(String name) {
        Object o = configurationModels.get(name);
        if (o != null) {
            File file = new File(ownPlugin.getDataFolder(), name + ".conf");
            FileUtils.writeFileContent(file, FileUtils.GSON.toJson(data.get(name)));
        }
    }

    public void saveFiles() {
        data.forEach((key, value) -> saveFile(key));
    }
}
