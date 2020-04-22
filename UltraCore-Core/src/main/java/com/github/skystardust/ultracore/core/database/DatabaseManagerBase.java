package com.github.skystardust.ultracore.core.database;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.skystardust.ultracore.core.PluginInstance;
import com.github.skystardust.ultracore.core.configuration.SQLConfiguration;
import com.github.skystardust.ultracore.core.exceptions.ConfigurationException;
import com.github.skystardust.ultracore.core.exceptions.DatabaseInitException;
import com.github.skystardust.ultracore.core.utils.FileUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.datasource.DataSourceConfig;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.DdlGenerator;
import io.ebeaninternal.server.core.DefaultServer;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.val;

import javax.annotation.Nonnull;
import java.io.File;
import java.lang.reflect.Field;
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
            if(sqlConfiguration.getAutoCommit()!=null)
            dataSourceConfig.setAutoCommit(sqlConfiguration.getAutoCommit());
            ServerConfig serverConfig = new ServerConfig();
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setUsername(sqlConfiguration.getUsername());
            hikariConfig.setPassword(sqlConfiguration.getPassword());
            hikariConfig.setJdbcUrl(sqlConfiguration.getUrl());
            hikariConfig.setDriverClassName(sqlConfiguration.getDriver());
            if(sqlConfiguration.getAutoCommit()!=null)
            hikariConfig.setAutoCommit(sqlConfiguration.getAutoCommit());
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            serverConfig.setName(name);
            //serverConfig.setObjectMapper(new ObjectMapper().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII,true));
            modelClass.forEach(serverConfig::addClass);
            serverConfig.setDataSourceConfig(dataSourceConfig);
            serverConfig.setDataSource(hikariDataSource);
            if(sqlConfiguration.getAutoCommit()!=null)
            serverConfig.setAutoCommitMode(sqlConfiguration.getAutoCommit());
            modelClass.forEach(c -> Thread.currentThread().setContextClassLoader(c.getClassLoader()));
            this.ebeanServer = EbeanServerFactory.create(serverConfig);
        } catch (Exception e) {
            throw new DatabaseInitException(e.getMessage(), e.getCause());
        }
        try {
            ebeanServer.find(modelClass.get(0)).setMaxRows(1).findOneOrEmpty();
        } catch (Exception e) {
            DefaultServer.class.cast(ebeanServer).executePlugins(false);
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