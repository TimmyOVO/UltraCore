package com.github.skystardust.ultracore.core.modules;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

@Getter
public class ModuleClassLoader extends URLClassLoader {
    private final File file;
    private AbstractModule instance;
    private String moduleName;

    public ModuleClassLoader(URL[] urls, ClassLoader parent, File file) {
        super(urls, parent);
        this.file = file;
        try {
            JarFile jarFile = new JarFile(file);
            Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                System.out.println("无法找到 " + file.getName() + " 的引导文件,跳过加载!");
                return;
            }
            String value = manifest.getMainAttributes().getValue("mod-main");
            if (value == null || value.isEmpty()) {
                System.out.println("无法找到 " + file.getName() + " 的引导属性,跳过加载!");
                return;
            }
            Class<?> aClass = Class.forName(value, true, this);
            if (aClass.getSuperclass() != AbstractModule.class) {
                System.out.println(file.getName() + " 未继承AbstractModule,跳过加载!");
                return;
            }
            Object o = aClass.getConstructor().newInstance();
            instance = (AbstractModule) o;
            this.moduleName = instance.getModuleName();
            Method onLoad = aClass.getMethod("onLoad");
            try {
                onLoad.invoke(o);
            } catch (Exception exx) {
                System.out.println("加载 " + moduleName + " 时出现了一个未知错误...");
                exx.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            System.out.println(file.getName() + " 无法实例化 ,缺少无参构造函数!");
        } catch (NoSuchMethodException ex) {
            System.out.println(file.getName() + " 无法找到onLoad方法,加载失败!");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            Method onDisable = instance.getClass().getMethod("onDisable");
            onDisable.invoke(instance);
        } catch (NoSuchMethodException ignored) {

        } catch (IllegalAccessException | InvocationTargetException e) {
            System.out.println(file.getName() + " onDisable调用失败...!");
            System.out.println(e.getLocalizedMessage());
        }
        super.close();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println(">>>>>>>>>>>> Loading class : " + name);
        return super.findClass(name);
    }
}
