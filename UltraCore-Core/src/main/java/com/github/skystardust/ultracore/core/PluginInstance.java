package com.github.skystardust.ultracore.core;

import java.io.File;
import java.util.logging.Logger;

public interface PluginInstance {
    Logger getPluginLogger();

    String getName();

    File getDataFolder();
}
