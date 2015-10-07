/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2013 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.webapp.main;

import org.apache.catalina.startup.Tomcat;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.common.ConstantValues;
import org.artifactory.util.PathUtils;
import org.artifactory.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author yoavl
 */
public class StartArtifactoryTomcatDev {

    public static final String DEFAULT_PREFIX = "..";

    /**
     * Main function, starts the Tomcat server.
     */
    public static void main(String... args) throws IOException {
        System.setProperty("java.net.preferIPv4Stack", "true");

        String prefix = args.length == 0 ? DEFAULT_PREFIX : args[0];

        // set home dir - dev mode only!
        // System.setProperty(ConstantValues.dev.getPropertyName(), "true");
        //In dev mod check for plugin updates frequently
        System.setProperty(ConstantValues.pluginScriptsRefreshIntervalSecs.getPropertyName(), "20");

        String homeProperty = System.getProperty("artifactory.home");
        File devArtHome = new File(
                homeProperty != null ? homeProperty : prefix + "/devenv/.artifactory").getCanonicalFile();
        if (!devArtHome.exists() && !devArtHome.mkdirs()) {
            throw new RuntimeException("Failed to create home dir: " + devArtHome.getAbsolutePath());
        }
        System.setProperty(ArtifactoryHome.SYS_PROP, devArtHome.getAbsolutePath());

        File standaloneSrcDir = new File(prefix + "/open/distribution/standalone/src").getCanonicalFile();
        File devEtcDir = new File(standaloneSrcDir, "test/etc");
        updateMimetypes(devEtcDir);
        copyNewerDevResources(devEtcDir, devArtHome);

        File devHaEtcDir = new File(standaloneSrcDir, "test/ha-etc");
        copyNewerHaDevResources(devHaEtcDir, devArtHome);

        // set the logback.xml
        System.setProperty("logback.configurationFile", new File(devArtHome + "/etc/logback.xml").getAbsolutePath());

        //Manually set the selector (needed explicitly here before any logger kicks in)
        // create the logger only after artifactory.home is set
        Tomcat tomcat = null;
        try {
            tomcat = new Tomcat();
            tomcat.setBaseDir(devArtHome + "/work");
            tomcat.addWebapp("/artifactory", new File(prefix, "./open/web/war/src/main/webapp").getAbsolutePath());
            tomcat.start();
            tomcat.getServer().await();
        } catch (Exception e) {
            System.err.println("Could not start the Tomcat server: " + e);
            if (tomcat != null) {
                try {
                    tomcat.stop();
                } catch (Exception e1) {
                    System.err.println("Unable to stop the Tomcat server: " + e1);
                }
            }
        }
    }

    static void copyNewerHaDevResources(File devHaEtcDir, File devArtHome) throws IOException {
        updateHaEtc(devHaEtcDir, devArtHome);
    }

    static void updateHaEtc(File devHaEtcDir, File devArtHome) throws IOException {
        File homeHaEtcDir = new File(devArtHome, "ha-etc");
        IOFileFilter fileFilter = new NewerFileFilter(devHaEtcDir, homeHaEtcDir);
        FileUtils.copyDirectory(devHaEtcDir, homeHaEtcDir, fileFilter, true);
    }

    /**
     * Copy newer files from the standalone dir to the working artifactory home dir
     */
    static void copyNewerDevResources(File devEtcDir, File artHome) throws IOException {
        File homeEtcDir = updateArtEtc(devEtcDir, new File(artHome, "etc"));
        createConfigBootstrap(homeEtcDir);
    }

    static void createConfigBootstrap(File homeEtcDir) {
        /**
         * If the bootstrap already exists, it means it's not the first startup, so don't keep the original config file
         * or the etc folder will flood with bootstrap files
         */
        if (new File(homeEtcDir, ArtifactoryHome.ARTIFACTORY_CONFIG_BOOTSTRAP_FILE).exists()) {
            new File(homeEtcDir, ArtifactoryHome.ARTIFACTORY_CONFIG_FILE).delete();
        }
    }

    static File updateArtEtc(File devEtcDir, File artEtc) throws IOException {
        IOFileFilter fileFilter = new NewerFileFilter(devEtcDir, artEtc);
        fileFilter = FileFilterUtils.makeSVNAware(fileFilter);
        FileUtils.copyDirectory(devEtcDir, artEtc, fileFilter, true);
        return artEtc;
    }

    static void updateMimetypes(File devEtcDir) {
        File defaultMimeTypes = ResourceUtils.getResourceAsFile("/META-INF/default/mimetypes.xml");
        File devMimeTypes = new File(devEtcDir, "mimetypes.xml");
        if (!devMimeTypes.exists() || defaultMimeTypes.lastModified() > devMimeTypes.lastModified()) {
            // override developer mimetypes file with newer default mimetypes file
            try {
                FileUtils.copyFile(defaultMimeTypes, devMimeTypes);
            } catch (IOException e) {
                System.err.println("Failed to copy default mime types file: " + e.getMessage());
            }
        }
    }

    private static class NewerFileFilter extends AbstractFileFilter {
        private final File srcDir;
        private final File destDir;

        public NewerFileFilter(File srcDir, File destDir) {
            this.srcDir = srcDir;
            this.destDir = destDir;
        }

        @Override
        public boolean accept(File srcFile) {
            if (srcFile.isDirectory()) {
                return true;    // don't exclude directories
            }
            String relativePath = PathUtils.getRelativePath(srcDir.getAbsolutePath(), srcFile.getAbsolutePath());
            File destFile = new File(destDir, relativePath);
            if (!destFile.exists() || srcFile.lastModified() > destFile.lastModified()) {
                return true;
            }
            return false;
        }
    }
}
