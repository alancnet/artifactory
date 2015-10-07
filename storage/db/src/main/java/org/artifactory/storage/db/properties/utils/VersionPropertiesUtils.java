package org.artifactory.storage.db.properties.utils;

import org.artifactory.addon.AddonsManager;
import org.artifactory.addon.ArtifactoryRunningMode;
import org.artifactory.api.context.ArtifactoryContext;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.storage.db.properties.model.DbProperties;
import org.artifactory.storage.db.version.ArtifactoryDBVersion;
import org.artifactory.version.ArtifactoryVersion;
import org.artifactory.version.ArtifactoryVersionReader;
import org.artifactory.version.CompoundVersionDetails;

/**
 * Date: 8/27/13 9:29 PM
 *
 * @author freds
 */
public abstract class VersionPropertiesUtils {

    /**
     * Compare the version of Artifactory running with the DB version, and return:<ul>
     * <li><b>true</b> : if all is OK (server can run) and no upgrades to do.</li>
     * <li><b>false</b> : if an upgrades needs to be applied.</li>
     * <li><b>IllegalStateException</b> : if the version are incompatible.</li>
     * </ul>
     *
     * @param dbVersionDetails The version found in Db props
     * @param versionDetails   The running version of Artifactory
     * @return true all ok, false upgrade needed
     * @throws java.lang.IllegalStateException
     *          If compatibility cannot be done
     */
    public static boolean isDbPropertiesVersionCompatible(
            CompoundVersionDetails dbVersionDetails, CompoundVersionDetails versionDetails) {
        // TODO: [by fsi] compare version type, impossible to run old version on new or vice-versa
        // TODO: [by fsi] Info should come from ArtifactoryDBVersion
        // For now just compare the version and then release number
        if (dbVersionDetails.getVersion().before(versionDetails.getVersion())) {
            // Need upgrade
            return false;
        }
        if (dbVersionDetails.getVersion().after(versionDetails.getVersion())) {
            // Cannot upgrade running below DB
            return true;
        }
        // If DB above all good
        return dbVersionDetails.getRevisionInt() >= versionDetails.getRevisionInt();
    }

    public static CompoundVersionDetails getBeforeAutoDbConvertVersionDetails() {
        // Before auto convert implemented
        ArtifactoryVersion firstDbVersion = ArtifactoryDBVersion.v100.getComparator().getFrom();
        return new CompoundVersionDetails(firstDbVersion, firstDbVersion.getValue(),
                "" + firstDbVersion.getRevision(), 0L);
    }

    public static CompoundVersionDetails getDbCompoundVersionDetails(DbProperties dbProperties) {
        if (dbProperties == null) {
            // First version with props and no value
            ArtifactoryVersion firstPropsDbVersion = ArtifactoryDBVersion.v101.getComparator().getFrom();
            return new CompoundVersionDetails(firstPropsDbVersion, firstPropsDbVersion.getValue(),
                    "" + firstPropsDbVersion.getRevision(), 0L);
        }
        return ArtifactoryVersionReader.getCompoundVersionDetails(
                dbProperties.getArtifactoryVersion(),
                getRevisionStringFromInt(dbProperties.getArtifactoryRevision()),
                "" + dbProperties.getArtifactoryRelease());
    }

    public static DbProperties createDbPropertiesFromVersion(CompoundVersionDetails versionDetails) {
        long installTime = System.currentTimeMillis();
        return new DbProperties(installTime,
                versionDetails.getVersionName(),
                versionDetails.getRevisionInt(),
                versionDetails.getTimestamp()
        );
    }

    private static ArtifactoryRunningMode getCurrentArtifactoryMode() {
        ArtifactoryContext context = ContextHelper.get();
        if (context == null) {
            return ArtifactoryRunningMode.OSS;
        }
        AddonsManager addonsManager = context.beanForType(AddonsManager.class);
        return addonsManager.getArtifactoryRunningMode();
    }

    private static String getRevisionStringFromInt(int rev) {
        if (rev <= 0 || rev == Integer.MAX_VALUE) {
            return "" + Integer.MAX_VALUE;
        }
        return "" + rev;
    }
}
