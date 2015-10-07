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

package org.artifactory.storage.db.build.service;

import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.*;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.binstore.BinaryInfo;
import org.artifactory.build.BuildInfoUtils;
import org.artifactory.build.BuildRun;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.StorageException;
import org.artifactory.storage.binstore.service.BinaryStore;
import org.artifactory.storage.build.service.BuildSearchCriteria;
import org.artifactory.storage.build.service.BuildStoreService;
import org.artifactory.storage.db.DbService;
import org.artifactory.storage.db.build.dao.BuildArtifactsDao;
import org.artifactory.storage.db.build.dao.BuildDependenciesDao;
import org.artifactory.storage.db.build.dao.BuildModulesDao;
import org.artifactory.storage.db.build.dao.BuildsDao;
import org.artifactory.storage.db.build.entity.*;
import org.artifactory.storage.db.util.blob.BlobWrapperFactory;
import org.jfrog.build.api.*;
import org.jfrog.build.api.release.PromotionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.*;

/**
 * Date: 11/14/12
 * Time: 12:42 PM
 *
 * @author freds
 */
@Service
public class BuildStoreServiceImpl implements BuildStoreService {
    private static final Logger log = LoggerFactory.getLogger(BuildStoreServiceImpl.class);

    private static final String EXPORTABLE_BUILD_VERSION = "v2";

    @Autowired
    private DbService dbService;

    @Autowired
    private BinaryStore binaryStore;

    @Autowired
    private BuildsDao buildsDao;

    @Autowired
    private BuildModulesDao buildModulesDao;

    @Autowired
    private BuildArtifactsDao buildArtifactsDao;

    @Autowired
    private BuildDependenciesDao buildDependenciesDao;

    @Autowired
    private BlobWrapperFactory blobsFactory;

    public static Date parseStringToDate(String dateString) {
        return new Date(BuildInfoUtils.parseBuildTime(dateString));
    }

    public static String formatDateToString(long buildStarted) {
        return BuildInfoUtils.formatBuildTime(buildStarted);
    }

    @Override
    public void addBuild(String buildJson) {
        try {
            addBuild(JacksonReader.bytesAsClass(buildJson.getBytes(Charset.forName("UTF-8")), Build.class));
        } catch (IOException e) {
            throw new StorageException("Could not parse JSON build " + buildJson, e);
        }
    }

    @Override
    public void addBuild(Build build) {
        try {
            String buildStarted = build.getStarted();
            Date parsedDate = parseStringToDate(buildStarted);

            // TODO: [by fsi] we are loosing the timezone information written in the JSON
            // Generates a big inconsistency between DB entry and JSON data

            BuildEntity dbBuild = new BuildEntity(dbService.nextId(), build.getName(), build.getNumber(),
                    parsedDate.getTime(),
                    build.getUrl(), System.currentTimeMillis(), build.getArtifactoryPrincipal(),
                    0L, null);
            long buildId = dbBuild.getBuildId();
            dbBuild.setProperties(createProperties(buildId, build));
            dbBuild.setPromotions(createPromotions(buildId, build));
            buildsDao.createBuild(dbBuild, blobsFactory.createJsonObjectWrapper(build));
            insertModules(buildId, build);
        } catch (SQLException e) {
            throw new StorageException("Could not insert build " + build, e);
        }

    }

    private ArrayList<BuildPromotionStatus> createPromotions(long buildId, Build build) {
        List<PromotionStatus> statuses = build.getStatuses();
        ArrayList<BuildPromotionStatus> buildPromotions;
        if (statuses != null && !statuses.isEmpty()) {
            buildPromotions = new ArrayList<>(statuses.size());
            for (PromotionStatus status : statuses) {
                buildPromotions.add(convertPromotionStatus(buildId, status));
            }
        } else {
            buildPromotions = new ArrayList<>(1);
        }
        return buildPromotions;
    }

    private BuildPromotionStatus convertPromotionStatus(long buildId, PromotionStatus status) {
        return new BuildPromotionStatus(buildId,
                status.getTimestampDate().getTime(),
                status.getUser(),
                status.getStatus(),
                status.getRepository(),
                status.getComment(),
                status.getCiUser());
    }

    private Set<BuildProperty> createProperties(long buildId, Build build) {
        Properties properties = build.getProperties();
        Set<BuildProperty> buildProperties;
        if (properties != null && !properties.isEmpty()) {
            buildProperties = new HashSet<>(properties.size());
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                buildProperties.add(
                        new BuildProperty(dbService.nextId(), buildId, entry.getKey().toString(),
                                entry.getValue().toString())
                );
            }
        } else {
            buildProperties = new HashSet<>(1);
        }
        return buildProperties;
    }

    private void insertModules(long buildId, Build build) throws SQLException {
        List<Module> modules = build.getModules();
        if (modules == null || modules.isEmpty()) {
            // Nothing to do here
            return;
        }
        for (Module module : modules) {
            BuildModule dbModule = new BuildModule(dbService.nextId(), buildId, module.getId());
            Properties properties = module.getProperties();
            Set<ModuleProperty> moduleProperties;
            if (properties != null && !properties.isEmpty()) {
                moduleProperties = Sets.newHashSetWithExpectedSize(properties.size());
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    moduleProperties.add(
                            new ModuleProperty(dbService.nextId(), dbModule.getModuleId(),
                                    entry.getKey().toString(),
                                    entry.getValue().toString())
                    );
                }
            } else {
                moduleProperties = Sets.newHashSetWithExpectedSize(1);
            }
            dbModule.setProperties(moduleProperties);
            buildModulesDao.createBuildModule(dbModule);

            List<Artifact> artifacts = module.getArtifacts();
            List<BuildArtifact> dbArtifacts;
            if (artifacts != null && !artifacts.isEmpty()) {
                dbArtifacts = Lists.newArrayListWithExpectedSize(artifacts.size());
                for (Artifact artifact : artifacts) {
                    // Artifact properties are not inserted in DB
                    dbArtifacts.add(new BuildArtifact(dbService.nextId(), dbModule.getModuleId(),
                            artifact.getName(), artifact.getType(), artifact.getSha1(), artifact.getMd5()));
                }
            } else {
                dbArtifacts = Lists.newArrayListWithExpectedSize(1);
            }
            buildArtifactsDao.createBuildArtifacts(dbArtifacts);

            List<Dependency> dependencies = module.getDependencies();
            List<BuildDependency> dbDependencies;
            if (dependencies != null && !dependencies.isEmpty()) {
                dbDependencies = Lists.newArrayListWithExpectedSize(dependencies.size());
                for (Dependency dependency : dependencies) {
                    // Dependency properties are not inserted in DB
                    dbDependencies.add(new BuildDependency(dbService.nextId(), dbModule.getModuleId(),
                            dependency.getId(), dependency.getScopes(), dependency.getType(),
                            dependency.getSha1(), dependency.getMd5()));
                }
            } else {
                dbDependencies = Lists.newArrayListWithExpectedSize(1);
            }
            buildDependenciesDao.createBuildDependencies(dbDependencies);
        }
    }

    @Override
    public void populateMissingChecksums(Build build) {
        List<Module> modules = build.getModules();
        if (modules != null && !modules.isEmpty()) {
            for (Module module : modules) {
                handleBeanPopulation(module.getArtifacts());
                handleBeanPopulation(module.getDependencies());
            }
        }
    }

    @Override
    public BuildRun getBuildRun(String buildName, String buildNumber, String buildStarted) {
        try {
            Date parsedDate = parseStringToDate(buildStarted);
            BuildEntity entity = buildsDao.findBuild(buildName, buildNumber, parsedDate.getTime());
            if (entity != null) {
                return getBuildRun(entity);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute find build for build" +
                    " name='" + buildName + "' number='" + buildNumber + "' start='" + buildStarted + "'", e);
        }
        return null;
    }

    @Override
    public Build getBuildJson(BuildRun buildRun) {
        try {
            long buildId = findIdFromBuildRun(buildRun);
            if (buildId > 0L) {
                return buildsDao.getJsonBuild(buildId, Build.class);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute get build JSON for build " + buildRun, e);
        }
        return null;
    }

    @Override
    public void renameBuild(BuildRun originalBuildRun, Build renamedBuild, String currentUser) {
        try {
            long buildId = findIdFromBuildRun(originalBuildRun);
            if (buildId > 0L) {
                buildsDao.rename(buildId, renamedBuild.getName(), blobsFactory.createJsonObjectWrapper(renamedBuild),
                        currentUser, System.currentTimeMillis());
            } else {
                throw new StorageException("Could not find build to rename " + originalBuildRun);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not rename build " + originalBuildRun, e);
        }
    }

    @Override
    public void addPromotionStatus(Build build, PromotionStatus promotion, String currentUser) {
        BuildRun buildRun = getBuildRun(build.getName(), build.getNumber(), build.getStarted());
        if (buildRun == null) {
            throw new StorageException("Could not add promotion " + promotion + " to non existent build " + build);
        }
        try {
            long buildId = findIdFromBuildRun(buildRun);
            build.addStatus(promotion);
            buildsDao.addPromotionStatus(buildId, convertPromotionStatus(buildId, promotion),
                    blobsFactory.createJsonObjectWrapper(build), currentUser, System.currentTimeMillis());
        } catch (SQLException e) {
            throw new StorageException("Could not add promotion " + promotion + " for build " + buildRun, e);
        }
    }

    @Override
    public String getBuildAsJson(BuildRun buildRun) {
        try {
            long buildId = findIdFromBuildRun(buildRun);
            if (buildId > 0L) {
                return buildsDao.getJsonBuild(buildId, String.class);
            }
        } catch (SQLException e) {
            throw new StorageException("Could not execute get JSON string for build " + buildRun, e);
        }
        return null;
    }

    @Override
    public Build getLatestBuild(String buildName, String buildNumber) {
        long buildDate;
        String fullBuildDate = "not found";
        try {
            buildDate = buildsDao.findLatestBuildDate(buildName, buildNumber);
            if (buildDate > 0L) {
                fullBuildDate = formatDateToString(buildDate);
                return getBuildJson(new BuildRunImpl(buildName, buildNumber, fullBuildDate));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not find build JSON for latest build" +
                    " name='" + buildName + "' number='" + buildNumber + "' latest date found='" + fullBuildDate + "'",
                    e
            );
        }
        return null;
    }

    @Override
    public ImportableExportableBuild getExportableBuild(BuildRun buildRun) {
        try {
            BuildEntity buildEntity = buildsDao.findBuild(buildRun.getName(), buildRun.getNumber(),
                    buildRun.getStartedDate().getTime());
            if (buildEntity == null) {
                throw new StorageException("Cannot create exportable build of non existent build " + buildRun);
            }
            ImportableExportableBuild exportedBuild = new ImportableExportableBuild();
            exportedBuild.setVersion(EXPORTABLE_BUILD_VERSION);
            exportedBuild.setBuildName(buildEntity.getBuildName());
            exportedBuild.setBuildNumber(buildEntity.getBuildNumber());
            exportedBuild.setBuildStarted(formatDateToString(buildEntity.getBuildDate()));

            String jsonString = buildsDao.getJsonBuild(buildEntity.getBuildId(), String.class);
            exportedBuild.setJson(jsonString);
            // TODO: Check if we need to keep checksums of the JSON ??
            //exportedBuild.setChecksumsInfo(content.getChecksums());

            exportedBuild.setCreated(buildEntity.getCreated());
            exportedBuild.setLastModified(buildEntity.getModified());
            exportedBuild.setCreatedBy(buildEntity.getCreatedBy());
            exportedBuild.setLastModifiedBy(buildEntity.getModifiedBy());

            return exportedBuild;
        } catch (SQLException e) {
            throw new StorageException("Could not create exportable build object for " + buildRun, e);
        }
    }

    @Override
    public void deleteAllBuilds(String buildName) {
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName);
            deleteBuilds(buildIds);
        } catch (SQLException e) {
            throw new StorageException("Could not delete all build with name='" + buildName + "'", e);
        }
    }

    private void deleteBuilds(Collection<Long> buildIds) throws SQLException {
        for (Long buildId : buildIds) {
            List<Long> moduleIds = buildModulesDao.findModuleIdsForBuild(buildId);
            if (!moduleIds.isEmpty()) {
                buildArtifactsDao.deleteBuildArtifacts(moduleIds);
                buildDependenciesDao.deleteBuildDependencies(moduleIds);
            }
            buildModulesDao.deleteBuildModules(buildId);
            buildsDao.deleteBuild(buildId);
        }
    }

    @Override
    public void deleteBuild(BuildRun buildRun) {
        log.debug("Deleting Build " + buildRun);
        try {
            long buildId = findIdFromBuildRun(buildRun);
            if (buildId > 0L) {
                deleteBuilds(ImmutableList.of(buildId));
            } else {
                log.info("Build " + buildRun + " already deleted!");
            }
        } catch (SQLException e) {
            throw new StorageException("Could not delete build " + buildRun, e);
        }
    }

    @Override
    public void deleteBuild(String buildName, String buildNumber, String buildStarted) {
        deleteBuild(new BuildRunImpl(buildName, buildNumber, buildStarted));
    }

    @Override
    public void deleteAllBuilds() {
        try {
            buildArtifactsDao.deleteAllBuildArtifacts();
            buildDependenciesDao.deleteAllBuildDependencies();
            buildModulesDao.deleteAllBuildModules();
            buildsDao.deleteAllBuilds();
        } catch (SQLException e) {
            throw new StorageException("Could not delete all builds", e);
        }
    }

    @Override
    public Set<BuildRun> findBuildsForChecksum(BuildSearchCriteria criteria, ChecksumType type, String checksum) {
        if (!type.isValid(checksum)) {
            log.info("Looking for invalid checksum " + type.name() + " '" + checksum + "'");
        }
        try {
            Set<BuildRun> results = Sets.newHashSet();
            if (criteria.searchInDependencies()) {
                Collection<BuildEntity> buildEntities = buildsDao.findBuildsForDependencyChecksum(type, checksum);
                for (BuildEntity buildEntity : buildEntities) {
                    results.add(getBuildRun(buildEntity));
                }
            }

            if (criteria.searchInArtifacts()) {
                Collection<BuildEntity> buildEntities = buildsDao.findBuildsForArtifactChecksum(type, checksum);
                for (BuildEntity buildEntity : buildEntities) {
                    results.add(getBuildRun(buildEntity));
                }
            }

            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not find builds for " + type.name() + " '" + checksum + "'", e);
        }
    }

    @Override
    public List<PublishedModule> getPublishedModules(String buildName, String date, String orderBy, String direction, String offset, String limit) {
        List<PublishedModule> buildModule = null;
        try {
             buildModule = buildsDao.getBuildModule(buildName, date, orderBy, direction, offset, limit);
        }catch (Exception e){
            log.error(e.toString());
        }
        return buildModule;
    }

    @Override
    public  List<ModuleArtifact> getModuleArtifact(String buildName,String buildNumber, String moduleId,String date, String orderBy, String direction, String offset, String limit) {
        List<ModuleArtifact> moduleArtifactList = null;
        try {
            moduleArtifactList = buildsDao.getModuleArtifact(buildName, buildNumber, date, moduleId, orderBy, direction, offset, limit);
        }catch (Exception e){
            log.error(e.toString());
        }
        return moduleArtifactList;
    }

    @Override
    public List<ModuleDependency> getModuleDependency(String buildNumber, String moduleId, String date, String orderBy, String direction, String offset, String limit) {
        List<ModuleDependency> moduleArtifactList = null;
        try {
            moduleArtifactList = buildsDao.getModuleDependency(buildNumber, date, moduleId, orderBy, direction, offset, limit);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return moduleArtifactList;
    }

    @Override
    public int getModuleDependenciesCount(String buildNumber, String moduleId, String date) {
        int totalArtifactCount = 0;
        try {
            totalArtifactCount = buildsDao.getModuleDependenciesCount(buildNumber, date, moduleId);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return totalArtifactCount;
    }

    @Override
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        return buildsDao.getModuleArtifactsForDiffWithPaging(buildParams, offset, limit);
    }

    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        return buildsDao.getPrevBuildsList(buildName, buildDate);
    }

    @Override
    public int getModuleArtifactsForDiffCount(BuildParams buildParams, String offset, String limit) {
        return buildsDao.getModuleArtifactsForDiffCount(buildParams, offset, limit);
    }

    @Override
    public List<BuildProps> getBuildPropsData(BuildParams buildParams, String offset, String limit, String orderBy) {
        return buildsDao.getBuildProps(buildParams, offset, limit, orderBy);
    }

    @Override
    public long getBuildPropsCounts(BuildParams buildParams) {
        return buildsDao.getBuildPropsCounts(buildParams);
    }

    @Override
    public int getPropsDiffCount(BuildParams buildParams) {
        return buildsDao.getPropsDiffCount(buildParams);
    }

    @Override
    public List<BuildProps> getBuildProps(BuildParams buildParams, String offset, String limit) {
        return buildsDao.diffBuildProps(buildParams, offset, limit);
    }


    @Override
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        return buildsDao.getModuleDependencyForDiffWithPaging(buildParams, offset, limit);
    }

    @Override
    public int getModuleDependencyForDiffCount(BuildParams buildParams, String offset, String limit) {
        return buildsDao.getModuleDependencyForDiffCount(buildParams, offset, limit);
    }


    @Override
    public  int getModuleArtifactCount(String buildNumber, String moduleId,String date) {
        int totalArtifactCount=0;
        try {
            totalArtifactCount =  buildsDao.getModuleArtifactCount(buildNumber, date, moduleId);
        }catch (Exception e){
            log.error(e.toString());
        }
        return totalArtifactCount;
    }


    @Override
    public int  getPublishedModulesCounts(String buildName, String date) {

        int  buildModuleCount = 0;
        try {
            buildModuleCount = buildsDao.getPublishedModulesCounts(buildName, date);
        }catch (Exception e){
            log.error(e.toString());
        }
        return buildModuleCount;
    }

    @Override
    public Set<BuildRun> getLatestBuildsByName() {
        try {
            List<String> allBuildNames = buildsDao.getAllBuildNames();
            LinkedHashSet<BuildRun> results = new LinkedHashSet<>(allBuildNames.size());
            for (String buildName : allBuildNames) {
                BuildEntity buildEntity = buildsDao.getLatestBuild(buildName);
                if (buildEntity != null) {
                    results.add(getBuildRun(buildEntity));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not list all builds by name and latest build date", e);
        }
    }

    @Override
    public Set<BuildRun> getLatestBuildsPaging(String offset, String orderBy, String direction, String limit) {
        try {
            List<BuildEntity> allBuildNames = buildsDao.getAllBuildNamePaging(offset, orderBy, direction, limit);
            LinkedHashSet<BuildRun> results = new LinkedHashSet<>(allBuildNames.size());
            for (BuildEntity buildEntity : allBuildNames) {
                if (buildEntity != null) {
                    results.add(getBuildRun(buildEntity));
                }
            }
            return results;
        } catch (SQLException e) {
            throw new StorageException("Could not list all builds by name and latest build date", e);
        }
    }

    @Override
    public Set<BuildRun> findBuildsByName(String buildName) {
        Set<BuildRun> results = Sets.newHashSet();
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName);
            for (Long buildId : buildIds) {
                results.add(getBuildRun(buildId));
            }
        } catch (SQLException e) {
            throw new StorageException("Could not search for builds with name='" + buildName + "'", e);
        }
        return results;
    }

    @Override
    public Set<BuildRun> findBuildsByNameAndNumber(String buildName, String buildNumber) {
        Set<BuildRun> results = Sets.newHashSet();
        try {
            List<Long> buildIds = buildsDao.findBuildIds(buildName, buildNumber);
            for (Long buildId : buildIds) {
                results.add(getBuildRun(buildId));
            }
        } catch (SQLException e) {
            throw new StorageException(
                    "Could not search for builds with name='" + buildName + "' and number='" + buildNumber + "'", e);
        }
        return results;
    }

    @Override
    public List<String> getAllBuildNames() {
        try {
            return buildsDao.getAllBuildNames();
        } catch (SQLException e) {
            throw new StorageException("Could not retrieve the list of build names", e);
        }
    }

    private long findIdFromBuildRun(BuildRun buildRun) throws SQLException {
        long buildId = 0L;
        if (buildRun instanceof BuildRunImpl) {
            buildId = ((BuildRunImpl) buildRun).getBuildId();
        }
        if (buildId <= 0L) {
            buildId = buildsDao.findBuildId(buildRun.getName(), buildRun.getNumber(),
                    buildRun.getStartedDate().getTime());
        }
        return buildId;
    }

    private BuildRun getBuildRun(Long buildId) throws SQLException {
        BuildEntity buildEntity = buildsDao.getBuild(buildId);
        return getBuildRun(buildEntity);
    }

    @Override
    public List<GeneralBuild> getBuildForNamePaging(String buildName, String orderBy, String direction, String offset, String limit) throws SQLException {
        return  buildsDao.getBuildForName(buildName, orderBy, direction, offset, limit);
    }

    @Override
    public int getBuildForNameTotalCount(String buildName) throws SQLException {
        return buildsDao.getBuildForNameTotalCount(buildName);
    }

    private BuildRun getBuildRun(BuildEntity buildEntity) {
        String releaseStatus = null;
        if (!buildEntity.getPromotions().isEmpty()) {
            releaseStatus = buildEntity.getPromotions().last().getStatus();
        }
        return new BuildRunImpl(buildEntity.getBuildId(), buildEntity.getBuildName(), buildEntity.getBuildNumber(),
                formatDateToString(buildEntity.getBuildDate()), buildEntity.getCiUrl(), releaseStatus);
    }

    /**
     * Locates and fills in missing checksums of a build file bean
     *
     * @param buildFiles List of build files to populate
     */
    private void handleBeanPopulation(List<? extends BuildFileBean> buildFiles) {
        if (buildFiles != null && !buildFiles.isEmpty()) {
            Set<String> checksums = Sets.newHashSet();
            for (BuildFileBean buildFile : buildFiles) {
                boolean sha1Exists = StringUtils.isNotBlank(buildFile.getSha1());
                boolean md5Exists = StringUtils.isNotBlank(buildFile.getMd5());

                //If the bean has both or none of the checksums, return
                if ((sha1Exists && md5Exists) || ((!sha1Exists && !md5Exists))) {
                    continue;
                }

                if (!sha1Exists) {
                    checksums.add(buildFile.getMd5());
                } else {
                    checksums.add(buildFile.getSha1());
                }
            }
            Set<BinaryInfo> binaryInfos = binaryStore.findBinaries(checksums);
            BiMap<String, String> found = HashBiMap.create(binaryInfos.size());
            for (BinaryInfo binaryInfo : binaryInfos) {
                found.put(binaryInfo.getSha1(), binaryInfo.getMd5());
            }
            for (BuildFileBean buildFile : buildFiles) {
                boolean sha1Exists = StringUtils.isNotBlank(buildFile.getSha1());
                boolean md5Exists = StringUtils.isNotBlank(buildFile.getMd5());

                //If the bean has both or none of the checksums, return
                if ((sha1Exists && md5Exists) || ((!sha1Exists && !md5Exists))) {
                    continue;
                }

                if (!sha1Exists) {
                    String newSha1 = found.inverse().get(buildFile.getMd5());
                    if (ChecksumType.sha1.isValid(newSha1)) {
                        buildFile.setSha1(newSha1);
                    }
                } else {
                    String newMd5 = found.get(buildFile.getSha1());
                    if (ChecksumType.md5.isValid(newMd5)) {
                        buildFile.setMd5(newMd5);
                    }
                }
            }
        }
    }

}