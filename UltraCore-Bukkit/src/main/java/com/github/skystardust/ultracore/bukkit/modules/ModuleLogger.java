package com.github.skystardust.ultracore.bukkit.modules;

import com.github.skystardust.ultracore.core.modules.AbstractModule;
import org.bukkit.Bukkit;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    private String moduleName;

    public ModuleLogger(AbstractModule context) {
        super(context.getClass().getCanonicalName(), null);
        this.moduleName = "[UltraCore-Module][" + context.getModuleName() + "]";
        this.setParent(Bukkit.getServer().getLogger());
        this.setLevel(Level.ALL);
    }

    public void log(LogRecord logRecord) {
        logRecord.setMessage(this.moduleName + logRecord.getMessage());
        super.log(logRecord);
    }
}
