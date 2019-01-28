package com.github.skystardust.ultracore.core.database.newgen;

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
import io.ebean.plugin.SpiServer;
import io.ebeaninternal.dbmigration.DdlGenerator;
import lombok.Getter;
import lombok.Setter;
import org.avaje.datasource.DataSourceConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter
@Setter
public class DatabaseManager {
    private SQLConfiguration sqlConfiguration;
    private EbeanServer ebeanServer;
    private ExecutorService executorService;

    private PluginInstance ownerPlugin;
    private List<Class<?>> modelClass;
    private String name;

    private DatabaseManager(PluginInstance plugin, SQLConfiguration sqlConfiguration) {
        this.ownerPlugin = plugin;
        this.sqlConfiguration = sqlConfiguration;
    }

    private DatabaseManager(Builder builder) {
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

    public static DatabaseManager createDatabaseManager(PluginInstance plugin, SQLConfiguration sqlConfiguration, List<Class<?>> modelClass, String name) throws DatabaseInitException {
        plugin.getPluginLogger().info("开始初始化 " + plugin.getName() + " 的 " + name + " 数据库!");
        plugin.getPluginLogger().info("版本: 3.0.x");
        return new DatabaseManager(plugin, sqlConfiguration).openConnection0(modelClass, name);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public DatabaseManager openConnection() throws DatabaseInitException {
        openConnection0(modelClass, name);
        return this;
    }

    private DatabaseManager openConnection0(List<Class<?>> modelClass, String name) throws DatabaseInitException {
        try {
            this.modelClass = modelClass;
            this.name = name;
            this.executorService = Executors.newCachedThreadPool();
            modelClass.forEach(cz -> {
                try {
                    URLClassLoader urlClassLoader = (URLClassLoader) cz.getClassLoader();
                    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                    method.setAccessible(true);
                    method.invoke(urlClassLoader, cz.getProtectionDomain().getCodeSource().getLocation());
                    getOwnerPlugin().getPluginLogger().info("已添加 " + cz.getName() + " !");
                } catch (Exception addURL) {
                    addURL.printStackTrace();
                }
            });
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
                ebeanServer.find(aClass).setMaxRows(1).findCount();
            }
        } catch (Exception exx) {
            SpiServer pluginApi = ebeanServer.getPluginApi();
            try {
                Field ddlGenerator = pluginApi.getClass().getDeclaredField("ddlGenerator");
                ddlGenerator.setAccessible(true);
                DdlGenerator ddlGenerator1 = (DdlGenerator) ddlGenerator.get(pluginApi);
                Method createAllDdl = ddlGenerator1.getClass().getDeclaredMethod("generateCreateAllDdl");
                createAllDdl.setAccessible(true);
                String invoke = (String) createAllDdl.invoke(ddlGenerator1);
                ddlGenerator1.runScript(false, invoke, "init_script");
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        getOwnerPlugin().getPluginLogger().info("初始化 " + name + " 的数据库成功!");
        return this;
    }

    public DatabaseManager reloadDatabase() throws ConfigurationException, DatabaseInitException {
        SQLConfiguration sqlConfiguration = DatabaseManager.setupDatabase(ownerPlugin);
        return DatabaseManager.createDatabaseManager(ownerPlugin, sqlConfiguration, modelClass, name);
    }

    public static final class Builder {
        private SQLConfiguration sqlConfiguration;
        private PluginInstance ownerPlugin;
        private List<Class<?>> modelClass;
        private String name;

        private Builder() {
        }

        public Builder withSqlConfiguration(SQLConfiguration val) {
            sqlConfiguration = val;
            return this;
        }

        public Builder withOwnerPlugin(PluginInstance val) {
            ownerPlugin = val;
            return this;
        }

        public Builder withModelClass(List<Class<?>> val) {
            modelClass = val;
            return this;
        }

        public Builder withName(String val) {
            name = val;
            return this;
        }

        public DatabaseManager build() {
            return new DatabaseManager(this);
        }
    }
}
