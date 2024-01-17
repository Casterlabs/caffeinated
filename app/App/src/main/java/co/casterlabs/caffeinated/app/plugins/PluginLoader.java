package co.casterlabs.caffeinated.app.plugins;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.reflections8.Reflections;

import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPluginImplementation;
import lombok.NonNull;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class PluginLoader {

    public static List<CaffeinatedPlugin> loadFile(@NonNull File file) throws IOException {
        if (file.isFile()) {
            URLClassLoader classLoader = null;

            try {
                URL url = file.toURI().toURL();

                classLoader = GlobalPluginClassLoader.create(url);

                List<Class<?>> types = new LinkedList<>();

                // Forcefully load all class files.
                // Reflections sux and doesn't work reliably enough.
                {
                    JarFile jarFile = new JarFile(file);
                    Enumeration<JarEntry> e = jarFile.entries();

                    while (e.hasMoreElements()) {
                        JarEntry je = e.nextElement();

                        if (je.isDirectory() || !je.getName().endsWith(".class")) {
                            continue;
                        }

                        // Transform the file name into a class name.
                        String className = je
                            .getName()
                            .substring(0, je.getName().length() - ".class".length())
                            .replace('/', '.');

                        Class<?> c = classLoader.loadClass(className);

                        if (c.isAnnotationPresent(CaffeinatedPluginImplementation.class)) {
                            types.add(c);
                        }
                    }

                    jarFile.close();
                }

                return loadFromClassCollection(types, classLoader);
            } catch (Exception e) {
                if (classLoader != null) {
                    classLoader.close();
                }

                throw new IOException("Unable to load file", e);
            }
        } else {
            throw new IOException("Target plugin must be a valid file");
        }
    }

    public static List<CaffeinatedPlugin> loadFromClassLoader(@NonNull PluginsHandler pluginsInst, @NonNull ClassLoader classLoader) throws IOException {
        Reflections reflections = new Reflections(classLoader);

        Set<Class<?>> types = reflections.getTypesAnnotatedWith(CaffeinatedPluginImplementation.class, true);

        // Frees an ungodly amount of ram, Reflections seems to be inefficient.
        reflections = null;
        System.gc();

        return loadFromClassCollection(types, classLoader);
    }

    public static List<CaffeinatedPlugin> loadFromClassCollection(@NonNull Collection<Class<?>> types, @NonNull ClassLoader classLoader) throws IOException {
        if (types.isEmpty()) {
            if (classLoader instanceof Closeable) {
                ((Closeable) classLoader).close();
            }

            classLoader = null;
            System.gc();

            throw new IOException("No implementations are present");
        } else {
            List<CaffeinatedPlugin> plugins = new LinkedList<>();

            for (Class<?> clazz : types) {
                if (CaffeinatedPlugin.class.isAssignableFrom(clazz)) {
                    try {
                        @SuppressWarnings("deprecation")
                        CaffeinatedPlugin plugin = (CaffeinatedPlugin) clazz.newInstance();
                        ServiceLoader<Driver> sqlDrivers = ServiceLoader.load(java.sql.Driver.class, classLoader);

                        ReflectionLib.setValue(plugin, "classLoader", classLoader);
                        ReflectionLib.setValue(plugin, "sqlDrivers", sqlDrivers);

                        // Load in the sql drivers.
                        for (Driver driver : sqlDrivers) {
                            driver.getClass().toString();
                        }

                        plugins.add(plugin);
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException | NoSuchFieldException e) {
                        throw new IOException("Unable to load plugin", e);
                    }
                }
            }

            return plugins;
        }
    }

}
