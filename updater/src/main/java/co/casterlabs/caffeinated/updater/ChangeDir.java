package co.casterlabs.caffeinated.updater;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class ChangeDir {

    public static void changeProcessDir(File newDirectory_) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        String newDirectory = newDirectory_.getAbsolutePath();

        // Use a native helper to get the real process moved.
        POSIX posix = POSIXFactory.getNativePOSIX();
        posix.chdir(newDirectory);

        // Update the older java.io static variables.
        Object /* ext. java.io.FileSystem */ ioFS = ReflectionLib.getStaticValue(File.class, "fs");
        ReflectionLib.setValue(ioFS, "userDir", newDirectory);

        // Need to figure out how to change this, it's a module so it'll be difficult.
//        ReflectionLib.setStaticValue(Class.forName("jdk.internal.util.StaticProperty"), "USER_DIR", newDirectory);

        // NIOFS is used by the Files class and some newer Java classes, so we gotta
        // update it too.
        FileSystem nioFS = FileSystems.getDefault();
        Field field_defaultDirectory = ReflectionLib.deepFieldSearch(nioFS.getClass(), "defaultDirectory");

        if (field_defaultDirectory != null) {
            // Note that we can skip all the normalization and checks that're normally
            // peformed because they already passed or were performed by the JVM when we
            // called getAbsolutePath();

            if (field_defaultDirectory.getType() == String.class) {
                // https://github.com/openjdk/jdk/blob/jdk-11%2B28/src/java.base/windows/classes/sun/nio/fs/WindowsFileSystem.java#L56

                ReflectionLib.setValue(nioFS, "defaultDirectory", newDirectory);
            }

            if (field_defaultDirectory.getType() == byte[].class) {
                // https://github.com/openjdk/jdk/blob/jdk-11%2B28/src/java.base/unix/classes/sun/nio/fs/UnixFileSystem.java#L51

                Charset jnuEncoding = Charset.forName(
                    System.getProperty("sun.jnu.encoding", "UTF-8")
                );

                byte[] bytes = newDirectory.getBytes(jnuEncoding);

                ReflectionLib.setValue(nioFS, "defaultDirectory", bytes);
            }
        }

        // Finally, update the property just to make sure.
        System.setProperty("user.dir", newDirectory);
    }

}
