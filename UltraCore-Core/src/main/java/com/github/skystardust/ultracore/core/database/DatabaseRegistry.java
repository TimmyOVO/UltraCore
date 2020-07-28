package com.github.skystardust.ultracore.core.database;

import com.github.skystardust.ultracore.core.database.newgen.DatabaseManager;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.exceptions.DatabaseInitException;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DatabaseRegistry {
    @Getter
    private static final Map<String, DatabaseManager> pluginDatabaseManagerBaseMap = new HashMap<>();

    public static void registerPluginDatabase(String plugin, DatabaseManager databaseManager) {
        DatabaseRegistry.pluginDatabaseManagerBaseMap.put(plugin, databaseManager);
    }

    public static void reloadPluginDatabase(String databaseName) throws DatabaseInitException, ConfigurationException {
        if (databaseName == null) {
            throw new DatabaseInitException("无法找到的数据库");
        }
        DatabaseManager databaseManager = pluginDatabaseManagerBaseMap.get(databaseName);
        if (databaseManager == null) {
            throw new DatabaseInitException("未注册的数据库");
        }
        if (!DatabaseListenerRegistry.hasDatabase(databaseName)) {
            throw new DatabaseInitException("该数据库不支持重载!");
        }
        DatabaseManager reloaded = databaseManager.reloadDatabase();
        registerPluginDatabase(databaseName, reloaded);
        DatabaseListenerRegistry.getPluginDatabaseListener(databaseName).ifPresent(databaseListener -> {
            databaseListener.notifyRealod(reloaded);
        });
    }
}
