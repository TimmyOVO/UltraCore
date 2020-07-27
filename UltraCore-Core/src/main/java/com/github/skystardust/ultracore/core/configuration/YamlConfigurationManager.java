package com.github.skystardust.ultracore.core.configuration;

import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.utils.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

public class YamlConfigurationManager extends ConfigurationManager {
    public YamlConfigurationManager(PluginInstance ownPlugin) {
        super(ownPlugin);
    }

    public ConfigurationClassSetter init(Class clazz, @Nullable Object o) {
        if (!getOwnPlugin().getDataFolder().exists()) {
            getOwnPlugin().getDataFolder().mkdirs();
        }
        setClassSetterInfo(new Map.Entry<Class<?>, Object>() {
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
        });
        getOwnPlugin().getPluginLogger().info("正在初始化 " + getOwnPlugin().getName() + " 的配置文件!");
        getConfigurationModels().forEach((fileName, result) -> {
            getOwnPlugin().getPluginLogger().info("初始化配置文件 " + fileName + " 中,请稍候..!");
            File file = new File(getOwnPlugin().getDataFolder(), fileName + ".yml");
            if (!file.exists()) {
                getOwnPlugin().getPluginLogger().info("正在创建配置文件 " + fileName + " 的模板.");
                FileUtils.writeYamlFileContent(file, FileUtils.GSON.toJson(result));
                getOwnPlugin().getPluginLogger().info("创建 " + fileName + " 的模板完成!");
            }
            getOwnPlugin().getPluginLogger().info("正在读取配置文件 " + fileName + " 的现有存档.");
            getData().put(fileName, FileUtils.GSON.fromJson(FileUtils.readYamlFileContent(file), result.getClass()));
            getOwnPlugin().getPluginLogger().info("读取配置文件 " + fileName + " 已成功.");
        });
        getOwnPlugin().getPluginLogger().info("初始化 " + getOwnPlugin().getName() + " 已全部成功!");
        return ConfigurationClassSetter.builder()
                .classToSet(clazz)
                .classInstance(o)
                .configurationData(getData()).build();
    }

    @Override
    public void saveFile(String name) {
        Object o = getConfigurationModels().get(name);
        if (o != null) {
            File file = new File(getOwnPlugin().getDataFolder(), name + ".yml");
            FileUtils.writeYamlFileContent(file, FileUtils.GSON.toJson(getData().get(name)));
        }
    }
}
