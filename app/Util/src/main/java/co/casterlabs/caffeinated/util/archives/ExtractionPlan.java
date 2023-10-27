package co.casterlabs.caffeinated.util.archives;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;

public interface ExtractionPlan {

    public boolean allowFile(String filename);

    public static ExtractionPlan all() {
        return (String filename) -> true;
    }

    public static ExtractionPlan simple(@NonNull String base) {
        return (String filename) -> {
            return filename.startsWith(base);
        };
    }

    /**
     * @param keep    Regex; Use null if all (non discard) files should be kept.
     * @param discard Regex; Use null if no files should be outright discarded.
     */
    public static ExtractionPlan complex(@NonNull String base, @Nullable String[] keep, @Nullable String[] discard) {
        return (String filename) -> {
            if (!filename.startsWith(base)) {
                return false;
            }

            if (discard != null) {
                for (String regex : discard) {
                    if (filename.matches(regex)) {
                        return false;
                    }
                }
            }

            // Null means ALL.
            if (keep == null) return true;

            for (String regex : keep) {
                if (filename.matches(regex)) {
                    return true;
                }
            }

            return false;
        };
    }

}
