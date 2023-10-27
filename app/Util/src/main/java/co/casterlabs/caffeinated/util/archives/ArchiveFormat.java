package co.casterlabs.caffeinated.util.archives;

import java.net.URI;

import org.jetbrains.annotations.Nullable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ArchiveFormat {
    // @formatter:off
    TAR_GZ(".tar.gz"),
    TAR_XZ(".tar.xz"),
    TAR   (".tar"),
   _7ZIP  (".7z"),
    ZIP   (".zip"),
    ;
    // @formatter:on

    public final String extension;

    public static @Nullable ArchiveFormat probeFormat(String location) {
        String pathname = URI.create(location)
            .getPath()
            .toLowerCase();

        for (ArchiveFormat f : ArchiveFormat.values()) {
            if (pathname.endsWith(f.extension)) {
                return f;
            }
        }

        return null;
    }

}
