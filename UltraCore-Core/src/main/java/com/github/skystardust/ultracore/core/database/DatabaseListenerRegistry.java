package com.github.skystardust.ultracore.core.database;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DatabaseListenerRegistry {
    @Getter
    private static final Map<String, DatabaseListener> pluginDatabaseListenerMap = new HashMap<>();

    public static void registerDatabase(String databaseName, DatabaseListener databaseManagerBase) {
        DatabaseListenerRegistry.pluginDatabaseListenerMap.put(databaseName, databaseManagerBase);
    }

    public static boolean hasDatabase(String databaseName) {
        return DatabaseListenerRegistry.pluginDatabaseListenerMap.get(databaseName) != null;
    }

    public static Optional<DatabaseListener> getPluginDatabaseListener(String plugin) {
        return hasDatabase(plugin) ? Optional.of(DatabaseListenerRegistry.pluginDatabaseListenerMap.get(plugin)) : Optional.empty();
    }
}
