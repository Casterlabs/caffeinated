package co.casterlabs.caffeinated.app.plugins;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

public class GlobalPluginClassLoader extends ClassLoader {
    public static final GlobalPluginClassLoader instance = new GlobalPluginClassLoader();

    private static final ClassLoader parentClassLoader = GlobalPluginClassLoader.class.getClassLoader();

    private static List<ChildClassLoader> children = new LinkedList<>();

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // This is a devilishly terrible idea.
        // Have all plugin classloaders be created through this class, and link them all
        // together so they can "see" eachother's classes (but not resources!).

        try {
            // Try the parent.
            return parentClassLoader.loadClass(name);
        } catch (ClassNotFoundException e) {

            // Search the children.
            for (ChildClassLoader child : children.toArray(new ChildClassLoader[0]) /* Copy */) {
                try {
                    return child.loadClassFromSelf(name, resolve);
                } catch (ClassNotFoundException | NullPointerException ignored) {}
            }

            throw e; // We could not find class, so we throw.
        }
    }

    /**
     * Make sure to call {@link URLClassLoader#close()} on it when you're done!
     */
    public static URLClassLoader create(URL url) {
        return new ChildClassLoader(url);
    }

    private static class ChildClassLoader extends URLClassLoader {

        public ChildClassLoader(URL url) {
            super(new URL[] {
                    url
            }, parentClassLoader);

            children.add(this);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

//            if (!name.startsWith("jdk.") && !name.startsWith("java.") && !name.startsWith("javax.") && !name.startsWith("sun.")) {
//                FastLogger.logStatic(LogLevel.DEBUG, "%s: requested class %s", this.getURLs()[0], name);
//            }

            try {
                // Try to load from our internal resources.
                return this.loadClassFromSelf(name, resolve);
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                // Phone the parent, we don't have it.
                return instance.loadClass(name, resolve); // This throws.
            }
        }

        protected Class<?> loadClassFromSelf(String name, boolean resolve) throws ClassNotFoundException {
            return super.loadClass(name, resolve);
        }

        @Override
        public void close() throws IOException {
            children.remove(this);
            super.close();
        }

    }

}
