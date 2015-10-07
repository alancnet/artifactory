/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
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

package org.artifactory.maven;

import com.google.common.collect.Lists;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.common.ConstantValues;
import org.artifactory.ivy.IvyNaming;
import org.artifactory.ivy.IvyService;
import org.artifactory.mime.MavenNaming;
import org.artifactory.mime.NamingUtils;
import org.artifactory.sapi.common.RepositoryRuntimeException;
import org.artifactory.util.PathUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.SortedSet;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Yoav Landman
 */
public abstract class MavenModelUtils {
    public static final String UTF8 = "utf-8";
    private static final Logger log = LoggerFactory.getLogger(MavenModelUtils.class);
    private static final DateTimeFormatter UNIQUE_SNAPSHOT_FORMATTER =
            DateTimeFormat.forPattern("yyyyMMdd.HHmmss").withZoneUTC();
    //Uses lazy evaluation of the version (+?)
    //see: http://www.regular-expressions.info/reference.html
    //For testing, see: http://www.cis.upenn.edu/~matuszek/General/RegexTester/regex-tester.html
    //Should be used with standard artifact names:
    //artifactId-version.ext
    //artifactId-version-classifier.ext
    //groups: 1-artifactId; 2-version; 5 or 7-classifier; 8-packaging (file extension).
    //note that you may have a multi-part version value, e.g. 1.0-rc3, in this case check that there
    //is a '-' with a value and that the classifier is not empty and simply append it.
    private static final Pattern ARTIFACT_NAME_PATTERN =
            Pattern.compile("(.+?)-(\\d.+?(-SNAPSHOT)?)(-(.+?))?(-(.+?))?\\.(\\w{1,}?)");

    private MavenModelUtils() {
        // utility class
    }

    /**
     * @param time Time to format
     * @return Maven unique snapshot version timestamp for the input date. For example: 20130603.113821
     */
    public static String dateToUniqueSnapshotTimestamp(long time) {
        return UNIQUE_SNAPSHOT_FORMATTER.print(time);
    }

    /**
     * @param dateTime Time to format
     * @return Maven unique snapshot timestamp for the input date. For example: 20130603.113821
     */
    public static Date uniqueSnapshotTimestampToDate(String dateTime) {
        return UNIQUE_SNAPSHOT_FORMATTER.parseDateTime(dateTime).toDate();
    }

    /**
     * Creates a maven <code>Metadata</code> out of a string.
     *
     * @param metadataAsString String representing content of maven-metadata.xml
     * @return Metadata object created from the input string
     * @throws java.io.IOException If the input string is not a valid maven metadata
     */
    public static Metadata toMavenMetadata(String metadataAsString) throws IOException {
        return toMavenMetadata(new StringReader(metadataAsString));
    }

    /**
     * Creates a maven <code>Metadata</code> out of an input stream and will close the stream.
     *
     * @param metadataStream An input stream representing content of maven-metadata.xml
     * @return Metadata object created from the input stream
     * @throws java.io.IOException If the input stream is not a valid maven metadata
     */
    public static Metadata toMavenMetadata(InputStream metadataStream) throws IOException {
        return toMavenMetadata(new InputStreamReader(metadataStream, UTF8));
    }

    /**
     * Creates a maven <code>Metadata</code> out of a <code>java.io.Reader</code> and will close the reader.
     *
     * @param reader Reader representing content of maven-metadata.xml
     * @return Metadata object created from the reader
     * @throws java.io.IOException If the input reader doesn't holds a valid maven metadata
     */
    public static Metadata toMavenMetadata(Reader reader) throws IOException {
        MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
        try {
            return metadataReader.read(reader, false);
        } catch (XmlPullParserException e) {
            throw new IOException("Failed to parse metadata: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Build custom maven-metadata.xml according to a specific version.
     *
     * @param moduleInfo The original {@code ModuleInfo} to assemble the maven metadata according to the same
     *                   gid,aid,and version, {@link org.apache.maven.artifact.repository.metadata.Versioning#setLastUpdatedTimestamp(java.util.Date)} is updated to now. and
     *                   the build number and timestamp in the {@link org.apache.maven.artifact.repository.metadata.Snapshot} is set according to the name.
     * @param fileName   The file name
     * @return The custom maven-metadata.xml
     */
    public static Metadata buildSnapshotMavenMetadata(ModuleInfo moduleInfo, String fileName) {
        Metadata metadata = new Metadata();
        metadata.setGroupId(moduleInfo.getOrganization());
        metadata.setArtifactId(moduleInfo.getModule());
        metadata.setVersion(moduleInfo.getBaseRevision() + "-" + moduleInfo.getFolderIntegrationRevision());
        Versioning versioning = new Versioning();
        metadata.setVersioning(versioning);
        versioning.setLastUpdatedTimestamp(new Date());
        Snapshot snapshot = new Snapshot();
        versioning.setSnapshot(snapshot);
        snapshot.setBuildNumber(MavenNaming.getUniqueSnapshotVersionBuildNumber(fileName));
        snapshot.setTimestamp(MavenNaming.getUniqueSnapshotVersionTimestamp(fileName));
        if (ConstantValues.mvnMetadataVersion3Enabled.getBoolean()) {
            SnapshotVersion snapshotVersion = new SnapshotVersion();
            snapshotVersion.setUpdated(StringUtils.remove(snapshot.getTimestamp(), '.'));
            snapshotVersion.setVersion(moduleInfo.getBaseRevision() + "-" +
                    moduleInfo.getFileIntegrationRevision());
            //Should always be a pom, since this method is called only by PropertiesAddonImpl.assembleDynamicMetadata
            snapshotVersion.setExtension(moduleInfo.getExt());
            versioning.setSnapshotVersions(Lists.newArrayList(snapshotVersion));
        }
        return metadata;
    }

    /**
     * Create xml string from the input <code>Metadata</code>.
     *
     * @param metadata Maven metadata object
     * @return Xml string for the input metadata
     */
    public static String mavenMetadataToString(Metadata metadata) throws IOException {
        MetadataXpp3Writer writer = new MetadataXpp3Writer();
        StringWriter stringWriter = new StringWriter();
        writer.write(stringWriter, metadata);
        return stringWriter.toString();
    }

    /**
     * @param artifactInfo Maven artifact info to build the model from
     * @return A maven {@link Model} matching the values of the maven artifact info.
     */
    public static Model toMavenModel(MavenArtifactInfo artifactInfo) {
        return new MavenPomBuilder().groupId(artifactInfo.getGroupId()).artifactId(artifactInfo.getArtifactId())
                .version(artifactInfo.getVersion()).packaging(artifactInfo.getType()).build();
    }

    public static String mavenModelToString(Model model) {
        MavenXpp3Writer writer = new MavenXpp3Writer();
        StringWriter stringWriter = new StringWriter();
        try {
            writer.write(stringWriter, model);
        } catch (IOException e) {
            throw new RepositoryRuntimeException("Failed to convert maven model to string", e);
        }
        return stringWriter.toString();
    }

    public static Model stringToMavenModel(String pomAsString) {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        StringReader pomStream = new StringReader(pomAsString);
        try {
            return reader.read(pomStream);
        } catch (Exception e) {
            throw new RepositoryRuntimeException("Failed to convert string to maven model", e);
        }
    }

    /**
     * @param pomInputStream Input stream of the pom content.
     * @return Maven artifact info built from the pom data.
     */
    public static MavenArtifactInfo mavenModelToArtifactInfo(InputStream pomInputStream)
            throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        InputStreamReader pomStream = new InputStreamReader(pomInputStream, UTF8);
        Model model = reader.read(pomStream);
        return mavenModelToArtifactInfo(model);
    }

    public static MavenArtifactInfo mavenModelToArtifactInfo(Model model) {
        Parent parent = model.getParent();
        String groupId = model.getGroupId();
        if (groupId == null && parent != null) {
            groupId = parent.getGroupId();
        }
        MavenArtifactInfo artifactInfo = new MavenArtifactInfo();
        artifactInfo.setGroupId(groupId);
        artifactInfo.setArtifactId(model.getArtifactId());
        String version = model.getVersion();
        if (version == null && parent != null) {
            version = parent.getVersion();
        }
        artifactInfo.setVersion(version);
        return artifactInfo;
    }

    /**
     * Returns a maven artifact info after gathering information from the given file
     *
     * @param file File to gather information from
     * @return MavenArtifactInfo object containing gathered information
     */
    public static MavenArtifactInfo artifactInfoFromFile(File file) {
        MavenArtifactInfo result;
        result = attemptToBuildInfoFromModel(file);
        if (result != null) {
            // built from model - most accurate, we're done
            return result;
        }

        // no info from a model, try to guess as good as possible based on the file name and path
        result = getInfoByMatching(file.getName());
        fillMissingRequiredFields(file, result);
        return result;
    }

    /**
     * Attempt to gather maven artifact information from the given file based on a model (pom, ivy etc.).
     *
     * @param file Uploaded file to gather info from
     * @return Maven artifact info based on the model, null if model not found or couldn't be parsed
     */
    private static MavenArtifactInfo attemptToBuildInfoFromModel(File file) {
        MavenArtifactInfo result = null;
        String fileName = file.getName();
        if (NamingUtils.isJarVariant(fileName)) {
            //File is a jar variant
            result = gatherInfoFromJarFile(file);
        } else if (MavenNaming.isClientOrServerPom(fileName)) {
            result = gatherInfoFromPomFile(file);
        } else if (IvyNaming.isIvyFileName(fileName)) {
            result = gatherInfoFromIvyFile(file);
        }
        return result;
    }

    /**
     * Gathers maven artifact information which was (or was not) managed to gather from the given Jar file
     *
     * @param file Jar file to gather info from
     */

    private static MavenArtifactInfo gatherInfoFromJarFile(File file) {
        MavenArtifactInfo artifactInfo = null;
        JarInputStream jis = null;
        JarEntry entry;
        try {
            //Create a stream and try to find the pom file within the jar
            jis = new JarInputStream(new FileInputStream(file));
            entry = getPomFile(jis);

            //If a valid pom file was found
            if (entry != null) {
                try {
                    //Read the uncompressed content
                    artifactInfo = mavenModelToArtifactInfo(jis);
                    artifactInfo.setType(PathUtils.getExtension(file.getPath()));
                } catch (Exception e) {
                    log.warn("Failed to read maven model from '" + entry.getName() + "'. Cause: " + e.getMessage() +
                            ".", e);
                    artifactInfo = null;
                }
            }
        } catch (IOException e) {
            log.warn("Failed to read maven model from '" + file + "'. Cause: " + e.getMessage() + ".", e);
        } finally {
            IOUtils.closeQuietly(jis);
        }
        return artifactInfo;
    }

    /**
     * Returns a JarEntry object if a valid pom file is found in the given jar input stream
     *
     * @param jis Input stream of given jar
     * @return JarEntry object if a pom file is found. Null if not
     * @throws IOException Any exceptions that might occur while using the given stream
     */
    private static JarEntry getPomFile(JarInputStream jis) throws IOException {
        if (jis != null) {
            JarEntry entry;
            while (((entry = jis.getNextJarEntry()) != null)) {
                String name = entry.getName();
                //Look for pom.xml in META-INF/maven/
                if (name.startsWith("META-INF/maven/") && name.endsWith("pom.xml")) {
                    return entry;
                }
            }
        }
        return null;
    }

    /**
     * @param file The file from which to try to extract the POM entry from.
     * @return The POM from the JAR in its String representation.
     */
    public static String getPomFileAsStringFromJar(File file) {
        JarEntry pomEntry;
        JarInputStream inputStream = null;
        try {
            inputStream = new JarInputStream(new FileInputStream(file));
            pomEntry = getPomFile(inputStream);
            if (pomEntry != null) {
                return IOUtils.toString(inputStream);
            }
        } catch (IOException e) {
            log.warn("Unable to read JAR to extract the POM from it.");
            // If the file has a corrupt file, the following error will be thrown.
            // See java.util.zip.ZipInputStream.getUTF8String()
        } catch (IllegalArgumentException iae) {
            log.warn("Unable to read JAR to extract the POM from it.");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    /**
     * Gathers maven artifact information which was (or was not) managed to gather from the given pom file
     *
     * @param file Jar file to gather info from
     * @return MavenArtifactInfo object to append info to, null if pom parsing failed
     */
    private static MavenArtifactInfo gatherInfoFromPomFile(File file) {
        MavenArtifactInfo result = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            result = mavenModelToArtifactInfo(in);
            result.setType(MavenArtifactInfo.POM);
        } catch (Exception e) {
            log.debug("Failed to read maven model from '{}'. Cause: {}.", file.getName(), e.getMessage());
        } finally {
            IOUtils.closeQuietly(in);
        }
        return result;
    }

    private static MavenArtifactInfo gatherInfoFromIvyFile(File file) {
        MavenArtifactInfo result = null;
        try {
            IvyService ivyService = ContextHelper.get().beanForType(IvyService.class);
            ModuleDescriptor ivyDescriptor = ivyService.parseIvyFile(file);
            if (ivyDescriptor != null) {
                ModuleRevisionId ivyModule = ivyDescriptor.getModuleRevisionId();
                result = new MavenArtifactInfo();
                result.setGroupId(ivyModule.getOrganisation());
                result.setArtifactId(ivyModule.getName());
                result.setVersion(ivyModule.getRevision());
                result.setClassifier("ivy");
                result.setType(MavenArtifactInfo.XML);
            } else {
                log.debug("Failed to read ivy model from '{}'", file.getName());
            }
        } catch (Exception e) {
            log.debug("Failed to read ivy model from '{}'. Cause: {}.", file.getName(), e.getMessage());
        }
        return result;
    }

    /**
     * Provides a fallback in order to fill in essential but missing information by using the given file's name
     *
     * @param file   Uploaded file to gather info from
     * @param result MavenArtifactInfo object to append info to
     */
    private static void fillMissingRequiredFields(File file, MavenArtifactInfo result) {
        String fileName = file.getName();
        String baseFileName = FilenameUtils.getBaseName(fileName);

        //Complete values by falling back to dumb defaults
        if (MavenArtifactInfo.NA.equals(result.getArtifactId())) {
            result.setArtifactId(baseFileName);
        }
        if (MavenArtifactInfo.NA.equals(result.getGroupId())) {
            //If we have no group, set it to be the same as the artifact name
            result.setGroupId(result.getArtifactId());
        }
        if (MavenArtifactInfo.NA.equals(result.getVersion())) {
            result.setVersion(baseFileName);
        }

        // fill the type if the extension is not null and the result holds the default (jar) or is NA
        String extension = PathUtils.getExtension(fileName);
        if (extension != null &&
                (MavenArtifactInfo.NA.equals(result.getType()) || MavenArtifactInfo.JAR.equals(result.getType()))) {
            result.setType(extension);
        }
    }

    /**
     * Returns a MavenArtifactInfo based on info that was managed to gather from the file name matcher
     *
     * @param fileName The file name (with no preceding path) to match against
     * @return MavenArtifactInfo object with gathered info
     */
    public static MavenArtifactInfo getInfoByMatching(String fileName) {
        MavenArtifactInfo mavenArtifactInfo = new MavenArtifactInfo();
        Matcher matcher = ARTIFACT_NAME_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            mavenArtifactInfo.setArtifactId(matcher.group(1));
            String version = matcher.group(2);
            if (StringUtils.isNotBlank(matcher.group(5)) && StringUtils.isNotBlank((matcher.group(7)))) {
                version += "-" + matcher.group(5);
            }
            mavenArtifactInfo.setVersion(version);
            String classifier;
            if (StringUtils.isNotBlank(matcher.group(5)) && StringUtils.isBlank(matcher.group(7))) {
                classifier = matcher.group(5);
            } else {
                classifier = matcher.group(7);
            }
            mavenArtifactInfo.setClassifier(classifier);
            mavenArtifactInfo.setType(matcher.group(8));
        }
        return mavenArtifactInfo;
    }

    /**
     * Returns a MavenArtifactInfo based on the supplied maven GAV coordinates, e.g. "group.id:artifactId:version"
     *
     * @param gav GAV, GAVC or GACV string
     * @return MavenMetadataInfo object with gathered info
     */
    @Nonnull
    public static MavenArtifactInfo getInfoFromGavString(String gav) {
        MavenArtifactInfo result = new MavenArtifactInfo();
        String[] splitId = gav.split(":");
        if (splitId.length == 4) {
            result.setGroupId(splitId[0]);
            result.setArtifactId(splitId[1]);
            result.setClassifier(splitId[2]);
            result.setVersion(splitId[3]);
        } else if (splitId.length == 3) {
            result.setGroupId(splitId[0]);
            result.setArtifactId(splitId[1]);
            result.setVersion(splitId[2]);
        }
        return result;
    }

    /**
     * Constructs GAV or GACV string representation of the given MavenArtifactInfo.
     *
     * @param info Maven info to use
     * @param gacv if true, creates "GroupId:ArtifactId:Classifier:Version" string, else create
     *             "GroupId:ArtifactId:Version"
     * @return GAV or GACV string
     */
    public static String getGavStringFromMavenInfo(MavenArtifactInfo info, boolean gacv) {
        StringBuilder builder = new StringBuilder(info.getGroupId());
        builder.append(":").append(info.getArtifactId());
        if (gacv) {
            builder.append(":").append(info.getClassifier());
        }
        return builder.append(":").append(info.getVersion()).toString();
    }

    public static Metadata buildReleasesMavenMetadata(String organization, String module,
            SortedSet<String> sortedVersions) {
        Metadata metadata = new Metadata();
        metadata.setGroupId(organization);
        metadata.setArtifactId(module);
        if (!sortedVersions.isEmpty()) {
            metadata.setVersion(sortedVersions.first());
            Versioning versioning = new Versioning();
            metadata.setVersioning(versioning);
            versioning.setVersions(Lists.newArrayList(sortedVersions));
            versioning.setLastUpdatedTimestamp(new Date());
            versioning.setLatest(sortedVersions.last());
            versioning.setRelease(sortedVersions.last());
        }
        return metadata;
    }
}
