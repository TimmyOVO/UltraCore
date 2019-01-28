package com.github.skystardust.ultracore.core.database;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.DataSourceConfig;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import com.avaje.ebeaninternal.server.ddl.DdlGenerator;
import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.configuration.SQLConfiguration;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.exceptions.DatabaseInitException;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class DatabaseManagerBase {
    private SQLConfiguration sqlConfiguration;
    private EbeanServer ebeanServer;
    private ExecutorService executorService;

    private PluginInstance ownerPlugin;
    private List<Class<?>> modelClass;
    private String name;

    private DatabaseManagerBase(PluginInstance plugin, SQLConfiguration sqlConfiguration) {
        this.ownerPlugin = plugin;
        this.sqlConfiguration = sqlConfiguration;
    }

    private DatabaseManagerBase(Builder builder) {
        setSqlConfiguration(builder.sqlConfiguration);
        setOwnerPlugin(builder.ownerPlugin);
        setModelClass(builder.modelClass);
        setName(builder.name);
    }

    public static SQLConfiguration setupDatabase(PluginInstance plugin) throws ConfigurationException {
        plugin.getPluginLogger().info("开始初始化 " + plugin.getName() + " 的配置文件!");
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            File sqlConfig = new File(plugin.getDataFolder(), "database.conf");
            if (!sqlConfig.exists()) {
                FileUtils.writeFileContent(sqlConfig, FileUtils.GSON.toJson(
                        SQLConfiguration.newBuilder()
                                .withUrl("jdbc:mysql://localhost:3306/database").withDriver("com.mysql.jdbc.Driver")
                                .withUsername("root")
                                .withPassword("pwd")
                                .build()
                ));
            }
            return FileUtils.GSON.fromJson(FileUtils.readFileContent(sqlConfig), SQLConfiguration.class);
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage(), e.getCause());
        }
    }

    public static DatabaseManagerBase createDatabaseManager(PluginInstance plugin, SQLConfiguration sqlConfiguration, List<Class<?>> modelClass, String name) throws DatabaseInitException {
        plugin.getPluginLogger().info("开始初始化 " + plugin.getName() + " 的 " + name + " 数据库!");
        plugin.getPluginLogger().info("版本: 2.0.x");
        return new DatabaseManagerBase(plugin, sqlConfiguration).openConnection0(modelClass, name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public DatabaseManagerBase openConnection() throws DatabaseInitException {
        openConnection0(modelClass, name);
        return this;
    }

    private DatabaseManagerBase openConnection0(List<Class<?>> modelClass, String name) throws DatabaseInitException {
        try {
            this.modelClass = modelClass;
            this.name = name;
            this.executorService = Executors.newCachedThreadPool();
            DataSourceConfig dataSourceConfig = new DataSourceConfig();
            dataSourceConfig.setUsername(sqlConfiguration.getUsername());
            dataSourceConfig.setPassword(sqlConfiguration.getPassword());
            dataSourceConfig.setUrl(sqlConfiguration.getUrl());
            dataSourceConfig.setDriver(sqlConfiguration.getDriver());
            ServerConfig serverConfig = new ServerConfig();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setUsername(sqlConfiguration.getUsername());
            hikariConfig.setPassword(sqlConfiguration.getPassword());
            hikariConfig.setJdbcUrl(sqlConfiguration.getUrl());
            hikariConfig.setDriverClassName(sqlConfiguration.getDriver());
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            serverConfig.setName(name);
            modelClass.forEach(serverConfig::addClass);
            serverConfig.setDataSourceConfig(dataSourceConfig);
            serverConfig.setDataSource(hikariDataSource);
            modelClass.forEach(c -> {
                Thread.currentThread().setContextClassLoader(c.getClassLoader());
            });
            this.ebeanServer = EbeanServerFactory.create(serverConfig);
        } catch (Exception e) {
            throw new DatabaseInitException(e.getMessage(), e.getCause());
        }
        try {
            for (Class<?> aClass : modelClass) {
                ebeanServer.find(aClass).setMaxRows(1).findUnique();
            }
        } catch (Exception e) {
            DdlGenerator gen = SpiEbeanServer.class.cast(ebeanServer).getDdlGenerator();
            gen.runScript(false, gen.generateCreateDdl());
        }
        getOwnerPlugin().getPluginLogger().info("初始化数据库 " + name + " 已成功!");
        return this;
    }

    public DatabaseManagerBase reloadDatabase() throws ConfigurationException, DatabaseInitException {
        SQLConfiguration sqlConfiguration = DatabaseManagerBase.setupDatabase(ownerPlugin);
        return DatabaseManagerBase.createDatabaseManager(ownerPlugin, sqlConfiguration, modelClass, name);
    }

    public static final class Builder {
        private SQLConfiguration sqlConfiguration;
        private PluginInstance ownerPlugin;
        private List<Class<?>> modelClass;
        private String name;

        private Builder() {
            this.modelClass = new ArrayList<>();
        }

        @Nonnull
        public Builder withSqlConfiguration(@Nonnull SQLConfiguration val) {
            sqlConfiguration = val;
            return this;
        }

        @Nonnull
        public Builder withOwnerPlugin(@Nonnull PluginInstance val) {
            ownerPlugin = val;
            return this;
        }

        @Nonnull
        public Builder withModelClass(@Nonnull Class<?> val) {
            modelClass.add(val);
            return this;
        }

        @Nonnull
        public Builder withName(@Nonnull String val) {
            name = val;
            return this;
        }

        @Nonnull
        public DatabaseManagerBase build() {
            return new DatabaseManagerBase(this);
        }
    }
}