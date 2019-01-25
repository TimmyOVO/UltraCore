package com.github.skystardust.ultracore.core.database;

import com.github.skystardust.ultracore.core.database.newgen.DatabaseManager;

public interface DatabaseListener {
    void notifyRealod(DatabaseManager databaseManager);
}
