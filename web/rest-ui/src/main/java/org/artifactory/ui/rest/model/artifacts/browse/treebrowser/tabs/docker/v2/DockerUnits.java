package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2;

/**
 * @author Shay Yaakov
 */
public abstract class DockerUnits {

    private DockerUnits() {
        // Utility class
    }

    // TODO: [by sy] StorageUnit? it's only 1024 there but I need 1000...
    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
