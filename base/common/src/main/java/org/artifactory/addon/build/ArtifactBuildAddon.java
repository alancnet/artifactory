package org.artifactory.addon.build;

import org.artifactory.addon.Addon;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.PublishedModule;
import org.artifactory.api.build.diff.BuildsDiffBaseFileModel;
import org.artifactory.api.build.diff.BuildsDiffPropertyModel;
import org.artifactory.api.rest.build.diff.BuildsDiff;
import org.artifactory.build.ArtifactoryBuildArtifact;
import org.artifactory.build.BuildRun;
import org.artifactory.fs.FileInfo;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.Dependency;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Chen Keinan
 */
public interface ArtifactBuildAddon extends Addon {

    /**
     * get build artifact info
     * @param build - build general info
     * @return map of build artifact info
     */
    Set<ArtifactoryBuildArtifact> getBuildArtifactsFileInfos(Build build);

    /**
     * get build dependencies file info
     * @param build - build general i nfo
     * @return map of build dependencies  file info
     */
    Map<Dependency, FileInfo> getBuildDependenciesFileInfos(Build build);

    /**
     * get alll latest builds with paging
     *
     * @param offset    - query offset
     * @param orderBy   -  query order by
     * @param direction - query direction
     * @param limit     - query limit
     * @return - set of builds
     */
    Set<BuildRun> getLatestBuildsPaging(String offset, String orderBy, String direction, String limit);

    /**
     * return list of Builds history by build name
     *
     * @param buildName - build name
     * @param orderBy   - order by
     * @param direction - direction (asc|desc)
     * @param offset    - offset start row
     * @param limit     - limit end row
     * @return list of build history
     * @throws SQLException
     */
    List<GeneralBuild> getBuildForNamePaging(String buildName, String orderBy, String direction, String offset, String limit) throws SQLException;

    /**
     * return to total count of historical Builds  for each name
     *
     * @return list of builds
     */
    int getBuildForNameTotalCount(String buildName) throws SQLException;

    Build getBuild(BuildRun build);

    BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted);

    Build getLatestBuildByNameAndNumber(String  buildName , String BuildNumber);

    List<PublishedModule> getPublishedModules(String buildName, String date, String orderBy, String direction, String offset, String limit);

    int  getPublishedModulesCounts(String buildName, String date);

    List<ModuleArtifact> getModuleArtifact(String buildName ,String buildNumber, String moduleId,String date, String orderBy, String direction, String offset, String limit);

   int getModuleArtifactCount(String buildNumber, String moduleId,String date);

    List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit);

    int getModuleDependencyCount(String buildNumber, String moduleId, String date);

    void deleteAllBuilds(String buildName);

     BuildsDiff getBuildsDiff(Build firstBuild, Build secondBuild, String baseStorageInfoUri);

    List<BuildsDiffBaseFileModel> compareArtifacts(Build build,Build secondBuild);

    List<BuildsDiffBaseFileModel> compareDependencies(Build build,Build secondBuild);

    List<BuildsDiffPropertyModel> compareProperties(Build build,Build secondBuild);
    }
