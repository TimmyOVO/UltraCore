package com.github.skystardust.ultracore.core.configuration;

import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.utils.FileUtils;

import java.io.File;

public class YamlConfigurationManager extends ConfigurationManager {
    public YamlConfigurationManager(PluginInstance ownPlugin) {
        super(ownPlugin);
    }


    @Override
    public void saveFile(String name) {
        Object o = getConfigurationModels().get(name);
        if (o != null) {
            File file = new File(getOwnPlugin().getDataFolder(), name + ".yml");
            writeConfigurationFile(file, jsonSerializer.toJson(getData().get(name)));
        }
    }

    @Override
    public void loadConfiguration(String fileName, Object defaultValue) {
        loadConfiguration(new File(getOwnPlugin().getDataFolder(), fileName + ".yml"), fileName, defaultValue);
    }

    public void writeConfigurationFile(File file, String content) {
        FileUtils.writeYamlFileContent(file, content);
    }

    public String readConfigurationFile(File file) {
        return FileUtils.readYamlFileContent(file);
    }

}
