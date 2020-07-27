package com.github.skystardust.ultracore.core.modules;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class JarModuleManager {
    private static Map<String, AbstractModule> modulesMap = new HashMap<>();

    public static Map<String, AbstractModule> getModulesMap() {
        return modulesMap;
    }

    public static void loadModules() {
        File file = new File(".", "uc-modules");
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
        Arrays.stream(Objects.requireNonNull(file.listFiles(), "no mod will be load"))
                .filter(file1 -> file1.getName().endsWith(".jar"))
                .forEach(JarModuleManager::loadModule);
    }

    public static boolean loadModule(File file) {
        System.out.println("开始加载 " + file.getName() + " ....");
        try {
            ModuleClassLoader moduleClassLoader = new ModuleClassLoader(new URL[]{file.toURI().toURL()}, JarModuleManager.class.getClassLoader(), file);
            modulesMap.put(moduleClassLoader.getModuleName(), moduleClassLoader.getInstance());
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static File unloadModule(String name) {
        Optional<AbstractModule> any = getModulesMap().entrySet()
                .stream()
                .filter(en -> en.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findAny();
        if (any.isPresent()) {
            ClassLoader classLoader = any.get().getClass().getClassLoader();
            if (classLoader instanceof ModuleClassLoader) {
                try {
                    ((ModuleClassLoader) classLoader).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getModulesMap().entrySet()
                        .stream()
                        .filter(en -> en.getKey().equalsIgnoreCase(name))
                        .findAny()
                        .ifPresent(entry -> modulesMap.remove(entry.getKey(), entry.getValue()));
                return ((ModuleClassLoader) classLoader).getFile();
            }
        }
        return null;
    }

    public static boolean reloadModule(String name) {
        File file = unloadModule(name);
        if (file != null) {
            return loadModule(file);
        }
        return false;
    }
}
