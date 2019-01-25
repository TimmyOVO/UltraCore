package com.github.skystardust.ultracore.nukkit;

import cn.nukkit.plugin.PluginBase;
import com.github.skystardust.ultracore.core.PluginInstance;

import java.util.logging.Logger;

public class UltraCore extends PluginBase implements PluginInstance {
    @Override
    public Logger getPluginLogger() {
        return Logger.getGlobal();
    }
}
