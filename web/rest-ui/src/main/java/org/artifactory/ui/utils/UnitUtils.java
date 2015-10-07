package org.artifactory.ui.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.artifactory.api.artifact.ArtifactInfo;
import org.artifactory.api.artifact.DebianArtifactInfo;
import org.artifactory.api.artifact.UnitInfo;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.maven.MavenService;
import org.artifactory.maven.MavenModelUtils;
import org.artifactory.mime.NamingUtils;
import org.artifactory.ui.rest.model.artifacts.deploy.UploadArtifactInfo;
import org.artifactory.util.PathUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Chen Keinan
 */
public class UnitUtils {

    private static final Logger log = LoggerFactory.getLogger(UnitUtils.class);

    /**
     * get maven artifact info from jar
     *
     * @param pomFromJar - pom content
     * @param file  - file
     * @return maven artifact info
     */
    public static UnitInfo getMavenArtifactInfo(File file, String pomFromJar) {
        if (pomFromJar == null) {
            if (file.getName().endsWith(".jar")) {
                return MavenModelUtils.artifactInfoFromFile(file);
            } else {
                return null;
            }
        }
        MavenArtifactInfo mavenArtifactInfo = null;
        if (StringUtils.isNotBlank(pomFromJar)) {
            InputStream pomInputStream = IOUtils.toInputStream(pomFromJar);
            try {
                mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
                if (mavenArtifactInfo == null && file.getName().endsWith(".jar")) {
                }
            } catch (IOException e) {
                log.error(e.toString());
            } catch (XmlPullParserException e) {
                log.error(e.toString());
            }
            mavenArtifactInfo.setType(PathUtils.getExtension(file.getName()));
        }
        return mavenArtifactInfo;
    }

    public static String getPomContent(File file, MavenArtifactInfo mavenArtifactInfo) {
        String pom = MavenModelUtils.getPomFileAsStringFromJar(file);
        if(StringUtils.isBlank(pom)){
            Model mavenModel = MavenModelUtils.toMavenModel(mavenArtifactInfo);
            pom = MavenModelUtils.mavenModelToString(mavenModel);
        }

        return pom;
    }

    /**
     * return unit info , 1st check if maven artifact info , if not return basic Artifact info
     *
     * @param file - current file
     * @param uploadArtifactInfo -
     * @param mavenService -
     * @return artifact info
     */
    public static UploadArtifactInfo getUnitInfo(File file, UploadArtifactInfo uploadArtifactInfo,
            MavenService mavenService) {
        UnitInfo artifactInfo = null;
        uploadArtifactInfo.cleanData();
        if (file.getName().endsWith(".pom")) {
            // TODO: [by dan] this could show more info (license etc.) - need to fix this
            MavenArtifactInfo pomInfo = getArtifactInfoFromPom(file, true);
            String pomFromPom = UnitUtils.getPomContent(file, pomInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromPom);
            artifactInfo = pomInfo;
        }
        if (file.getName().endsWith(".deb")) {
            artifactInfo = new DebianArtifactInfo(file.getName());
        }
        //if Maven
        if (NamingUtils.isJarVariant(file.getName())) {
            MavenArtifactInfo mavenArtifactInfo = mavenService.getMavenArtifactInfo(file);
            String pomFromJar = UnitUtils.getPomContent(file, mavenArtifactInfo);
            uploadArtifactInfo.setUnitConfigFileContent(pomFromJar);
            artifactInfo = getMavenArtifactInfo(file, pomFromJar);
        }

        if(artifactInfo == null){
            artifactInfo = new ArtifactInfo(file.getName());
        }

        uploadArtifactInfo.setUnitInfo(artifactInfo);

        return uploadArtifactInfo;
    }

    /**
     * get artifact info from pom
     *
     * @param file - current file
     * @return artifact info
     */
    private static MavenArtifactInfo getArtifactInfoFromPom(File file, boolean isPom) {
        MavenArtifactInfo mavenArtifactInfo = null;
        try {
            InputStream pomInputStream = new FileInputStream(file);
            mavenArtifactInfo = MavenModelUtils.mavenModelToArtifactInfo(pomInputStream);
            if (isPom) {
                mavenArtifactInfo.setType("pom");
            }
        } catch (FileNotFoundException e) {
            log.error(e.toString());
        } catch (XmlPullParserException e) {
            log.error(e.toString());
        } catch (IOException e) {
            log.error(e.toString());
        }
        return mavenArtifactInfo;
    }
}
