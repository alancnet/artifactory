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

package org.artifactory.storage.db.build.dao;

import com.google.common.collect.Lists;
import org.apache.commons.compress.utils.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildProps;
import org.artifactory.api.build.GeneralBuild;
import org.artifactory.api.build.ModuleArtifact;
import org.artifactory.api.build.ModuleDependency;
import org.artifactory.api.build.PublishedModule;
import org.artifactory.api.build.diff.BuildParams;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.checksum.ChecksumType;
import org.artifactory.storage.db.build.entity.BuildEntity;
import org.artifactory.storage.db.build.entity.BuildPromotionStatus;
import org.artifactory.storage.db.build.entity.BuildProperty;
import org.artifactory.storage.db.util.BaseDao;
import org.artifactory.storage.db.util.DbUtils;
import org.artifactory.storage.db.util.JdbcHelper;
import org.artifactory.storage.db.util.blob.BlobWrapper;
import org.artifactory.storage.db.util.querybuilder.QueryWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Date: 10/30/12
 * Time: 12:44 PM
 *
 * @author freds
 */
@Repository
public class BuildsDao extends BaseDao {
    private static final Logger log = LoggerFactory.getLogger(BuildsDao.class);

    @Autowired
    public BuildsDao(JdbcHelper jdbcHelper) {
        super(jdbcHelper);
    }

    public int createBuild(BuildEntity b, BlobWrapper jsonBlob) throws SQLException {
        int res = jdbcHelper.executeUpdate("INSERT INTO builds VALUES(" +
                "?, " +
                "?, ?, ?, " +
                "?, ?, ?," +
                "?, ?)",
                b.getBuildId(),
                b.getBuildName(), b.getBuildNumber(), b.getBuildDate(),
                b.getCiUrl(), b.getCreated(), b.getCreatedBy(),
                nullIfZero(b.getModified()), b.getModifiedBy());
        res += jdbcHelper.executeUpdate("INSERT INTO build_jsons VALUES(?,?)", b.getBuildId(), jsonBlob);
        int nbProps = b.getProperties().size();
        if (nbProps != 0) {
            for (BuildProperty bp : b.getProperties()) {
                String propValue = bp.getPropValue();
                if (propValue.length() > 2048) {
                    log.info("Trimming property value to 2048 characters {}", bp.getPropKey());
                    log.debug("Trimming property value to 2048 characters {}: {}", bp.getPropKey(), bp.getPropValue());
                    propValue = StringUtils.substring(propValue, 0, 2048);
                }
                res += jdbcHelper.executeUpdate("INSERT INTO build_props VALUES (?,?,?,?)",
                        bp.getPropId(), bp.getBuildId(), bp.getPropKey(), propValue);
            }
        }
        int nbPromotions = b.getPromotions().size();
        if (nbPromotions != 0) {
            for (BuildPromotionStatus bp : b.getPromotions()) {
                res += jdbcHelper.executeUpdate("INSERT INTO build_promotions VALUES (?,?,?,?,?,?,?)",
                        bp.getBuildId(), bp.getCreated(), bp.getCreatedBy(),
                        bp.getStatus(), bp.getRepository(), bp.getComment(), bp.getCiUser());
            }
        }
        return res;
    }

    public int rename(long buildId, String newName, BlobWrapper jsonBlob, String currentUser, long currentTime)
            throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE builds SET" +
                " build_name = ?, modified = ?, modified_by = ?" +
                " WHERE build_id = ?", newName, currentTime, currentUser, buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM build_jsons WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("INSERT INTO build_jsons VALUES(?,?)", buildId, jsonBlob);
        return res;
    }

    public int addPromotionStatus(long buildId, BuildPromotionStatus promotionStatus,
            BlobWrapper jsonBlob, String currentUser, long currentTime)
            throws SQLException {
        int res = jdbcHelper.executeUpdate("UPDATE builds SET" +
                " modified = ?, modified_by = ?" +
                " WHERE build_id = ?", currentTime, currentUser, buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM build_jsons WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("INSERT INTO build_jsons VALUES(?,?)", buildId, jsonBlob);
        res += jdbcHelper.executeUpdate("INSERT INTO build_promotions VALUES (?,?,?,?,?,?,?)",
                promotionStatus.getBuildId(),
                promotionStatus.getCreated(),
                promotionStatus.getCreatedBy(),
                promotionStatus.getStatus(),
                promotionStatus.getRepository(),
                promotionStatus.getComment(),
                promotionStatus.getCiUser());
        return res;
    }

    public int deleteAllBuilds() throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM build_jsons");
        res += jdbcHelper.executeUpdate("DELETE FROM build_props");
        res += jdbcHelper.executeUpdate("DELETE FROM build_promotions");
        res += jdbcHelper.executeUpdate("DELETE FROM builds");
        return res;
    }

    public int deleteBuild(long buildId) throws SQLException {
        int res = jdbcHelper.executeUpdate("DELETE FROM build_jsons WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM build_props WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM build_promotions WHERE build_id=?", buildId);
        res += jdbcHelper.executeUpdate("DELETE FROM builds WHERE build_id=?", buildId);
        return res;
    }

    public <T> T getJsonBuild(long buildId, Class<T> clazz) throws SQLException {
        ResultSet rs = null;
        InputStream jsonStream = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT build_info_json FROM build_jsons WHERE" +
                    " build_id = ?",
                    buildId);
            if (rs.next()) {
                jsonStream = rs.getBinaryStream(1);
                if (CharSequence.class.isAssignableFrom(clazz)) {
                    //noinspection unchecked
                    return (T) IOUtils.toString(jsonStream, Charsets.UTF_8.name());
                }
                return JacksonReader.streamAsClass(jsonStream, clazz);
            }
        } catch (IOException e) {
            throw new SQLException("Failed to read JSON data for build '" + buildId + "' due to: " + e.getMessage(), e);
        } finally {
            DbUtils.close(rs);
            IOUtils.closeQuietly(jsonStream);
        }
        return null;
    }

    public BuildEntity getBuild(long buildId) throws SQLException {
        ResultSet rs = null;
        BuildEntity build = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM builds WHERE" +
                    " build_id = ?",
                    buildId);
            if (rs.next()) {
                build = resultSetToBuild(rs);
            }
        } finally {
            DbUtils.close(rs);
        }
        if (build != null) {
            build.setProperties(findBuildProperties(build.getBuildId()));
            build.setPromotions(findBuildPromotions(build.getBuildId()));
        }
        return build;
    }


    /**
     * return list of previous build
     *
     * @param buildName - current build name
     * @param buildName - current build date
     * @return
     * @throws SQLException
     */
    public List<GeneralBuild> getPrevBuildsList(String buildName, String buildDate) {
        ResultSet rs = null;
        List<GeneralBuild> buildList = new ArrayList<>();
        try {
            String buildsQuery = "select * from builds  where build_name = ? and build_date < ?  order by build_number desc";
            rs = jdbcHelper.executeSelect(buildsQuery, buildName, Long.parseLong(buildDate));
            while (rs.next()) {
                Long id = rs.getLong(1);
                GeneralBuild buildEntity = resultSetToGeneralBuild(rs, id);
                buildList.add(buildEntity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally{
            DbUtils.close(rs);
        }
        return buildList;
    }


    public List<GeneralBuild> getBuildForName(String buildName, String orderBy, String direction, String offset, String limit) throws SQLException {
        ResultSet rs = null;
        List<GeneralBuild> buildList = new ArrayList<>();
        try {
            String buildsQuery = "select builds.* ,builds.build_id,\n" +
                    "         '0' as module_cnt ,\n" +
                    "         '0' as artifact_cnt,\n" +
                    "         '0' as dependency_cnt\n" +
                    "        ,  build_promotions.status,build_promotions.created as created2 from builds left join build_promotions on build_promotions.build_id = builds.build_id where build_name = ?";
            rs = jdbcHelper.executeSelect(buildsQuery, buildName);
            Map<Long, GeneralBuild> buildMap = new HashMap<>();
            while (rs.next()) {
                Long id = rs.getLong(1);
                Long created = rs.getLong(15);
                if (buildMap.get(id) == null) {
                    GeneralBuild buildEntity = resultSetToGeneralBuild(rs, id);
                    buildEntity.setNumOfModules(new Integer(rs.getInt(11)).toString());
                    buildEntity.setNumOfArtifacts(new Integer(rs.getInt(12)).toString());
                    buildEntity.setNumOfDependencies(new Integer(rs.getInt(13)).toString());
                    buildEntity.setStatus(rs.getString(14));
                    buildEntity.setPromotionCreated(created);
                    buildMap.put(id, buildEntity);
                    buildList.add(buildEntity);
                } else {
                    Long promotionCreated = buildMap.get(id).getPromotionCreated();
                    if (promotionCreated != null && promotionCreated < created) {
                        buildMap.get(id).setStatus(rs.getString(14));
                    }
                }
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildList;
    }

    /**
     * get Module Artifact diff with paging
     *
     * @param offset - row offset
     * @param limit  - row limit
     * @return
     */
    public List<ModuleArtifact> getModuleArtifactsForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        ResultSet rs = null;
        List<ModuleArtifact> artifacts = new ArrayList<>();
        Map<String, ModuleArtifact> artifactMap = new HashMap<>();
        ResultSet rsArtCurr = null;
        ResultSet rsArtPrev = null;
        try {
            Object[] diffParams = getArtifatBuildQueryParam(buildParams);
            String buildQuery = getArtifactBuildDiffQuery(buildParams);
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            while (rs.next()) {
                ModuleArtifact artifact = new ModuleArtifact(null, null, rs.getString(1), rs.getString(2), rs.getString(3));
                artifact.setStatus(rs.getString(4));
                if (buildParams.isAllArtifact()) {
                    artifact.setModule(rs.getString(5));
                }
                artifacts.add(artifact);
            }
            // update artifact repo path data
            if (!artifacts.isEmpty()) {
                rsArtCurr = getArtifactNodes(buildParams.getBuildName(), buildParams.getCurrBuildNum(), artifactMap);
                if (buildParams.isAllArtifact()) {
                    rsArtPrev = getArtifactNodes(buildParams.getBuildName(), buildParams.getComperedBuildNum(), artifactMap);
                }
                for (ModuleArtifact artifact : artifacts) {
                    ModuleArtifact moduleArtifact = artifactMap.get(artifact.getSha1());
                    if (moduleArtifact != null) {
                        artifact.setRepoKey(moduleArtifact.getRepoKey());
                        artifact.setPath(moduleArtifact.getPath());
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rsArtCurr);
            DbUtils.close(rsArtPrev);
            DbUtils.close(rs);
        }
        return artifacts;
    }

    private String getArtifactBuildDiffQuery(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return BuildQueries.MODULE_ARTIFACT_DIFF_QUERY;
        } else {
            return BuildQueries.BUILD_ARTIFACT_DIFF_QUERY;
        }
    }

    /**
     * return diif param for artifact diff query
     * @param buildParams
     * @return
     */
    private Object[] getArtifatBuildQueryParam(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};
        } else {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
        }
    }

    /**
     * get Module Artifact diff total Count
     *
     * @param offset - row offset
     * @param limit  - row limit
     * @return
     */
    public int getModuleArtifactsForDiffCount(BuildParams buildParams, String offset, String limit) {
        ResultSet rs = null;
        try {
            Object[] diffParams = getArtifactDiffCountParam(buildParams);
            String buildQuery = getArtifactDiffCount(buildParams);
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get Module Artifact diff total Count
     *
     * @param offset - row offset
     * @param limit  - row limit
     * @return
     */
    public List<BuildProps> getBuildProps(BuildParams buildParams,
                                          String offset, String limit, String orderBy) {
        ResultSet rs = null;
        List<BuildProps> buildPropsList = new ArrayList<>();
        try {
            String baseQuery;
            Object[] diffParams = getBuildPropsParam(buildParams);
            String buildQuery = getPropsQuery(buildParams);
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            while (rs.next()) {
                buildPropsList.add(new BuildProps(rs.getString(1), rs.getString(2), null));
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return buildPropsList;
    }


    /**
     * get build props row count
     *
     * @return
     */
    public long getBuildPropsCounts(BuildParams buildParams) {
        ResultSet rs = null;
        try {
            Object[] diffParams = getBuildPropsParam(buildParams);
            String buildQuery = getPropsQueryCounts(buildParams);
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get build props (env or system) param to be included in sql query
     *
     * @param buildParams - build params
     * @return list of build props param
     */
    private Object[] getBuildPropsParam(BuildParams buildParams) {
        if (buildParams.isEnvProps()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()};
        } else {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()
            };
        }
    }

    /**
     * get props Env or system query
     *
     * @param buildParams - build Param
     * @return build props query
     */
    private String getPropsQuery(BuildParams buildParams) {
        String baseQuery;
        if (buildParams.isEnvProps()) {
            baseQuery = BuildQueries.BUILD_ENV_PROPS;
        } else {
            baseQuery = BuildQueries.BUILD_SYSTEM_PROPS;
        }
        return baseQuery;
    }

    /**
     * get props Env or system query
     *
     * @param buildParams - build Param
     * @return build props query
     */
    private String getPropsQueryCounts(BuildParams buildParams) {
        String baseQuery;
        if (buildParams.isEnvProps()) {
            baseQuery = BuildQueries.BUILD_ENV_PROPS_COUNT;
        } else {
            baseQuery = BuildQueries.BUILD_SYSTEM_PROPS_COUNT;
        }
        return baseQuery;
    }

    /**
     * get Module Artifact diff total Count
     *
     * @param buildParams - encapsulate build diff query param
     * @return
     */
    public int getPropsDiffCount(BuildParams buildParams) {
        ResultSet rs = null;
        try {
            String baseQuery;
            Object[] diffParams = {buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
            String buildQuery = BuildQueries.BUILD_PROPS_COUNT;
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get build props diff with another build
     *
     * @return - list of build props with diff status
     */
    public List<BuildProps> diffBuildProps(BuildParams buildParams, String offset, String limit) {
        ResultSet rs = null;
        List<BuildProps> buildPropsList = new ArrayList<>();
        try {
            Object[] diffParams = {buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
            String buildQuery = BuildQueries.BUILD_PROPS_DIFF;
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            while (rs.next()) {
                BuildProps buildProps = new BuildProps(rs.getString(1), rs.getString(2), rs.getString(3));
                buildProps.setPrevValue(rs.getString(4));
                buildPropsList.add(buildProps);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return buildPropsList;
    }

    /**
     * get Build Artifact Diff count param array (for module artifact / all artifact)
     *
     * @param buildParams - build diff param for query
     * @return - param array for diff
     */
    private Object[] getArtifactDiffCountParam(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};

        } else {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
        }
    }

    /**
     * get Build Artifact Diff count query (for module artifact / all artifact)
     * @param buildParams - build diff param for query
     * @return - query for diff
     */
    private String getArtifactDiffCount(BuildParams buildParams) {
        String baseQuery;
        if (!buildParams.isAllArtifact()) {
            baseQuery = BuildQueries.MODULE_ARTIFACT_DIFF_COUNT;
        } else {
            baseQuery = BuildQueries.BUILD_ARTIFACT_DIFF_COUNT;
        }
        return baseQuery;
    }


    /**
     * get Module Artifact diff with paging
     *
     * @param offset - row offset
     * @param limit  - row limit
     * @return
     */
    public List<ModuleDependency> getModuleDependencyForDiffWithPaging(BuildParams buildParams, String offset, String limit) {
        ResultSet rs = null;
        ResultSet rsDep = null;
        ResultSet rsDepCompared = null;
        List<ModuleDependency> dependencies = new ArrayList<>();
        Map<String, ModuleDependency> moduleDependencyMap = new HashMap<>();
        try {
            StringBuilder builder = new StringBuilder(getBaseDependencyQuery(buildParams));
            Object[] diffParams = getBuildDependencyParams(buildParams);
            /// update query with specific conditions
            updateQueryWithSpecificConditions(buildParams, builder);
            String buildQuery = builder.toString();
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            Map<String, String> tempDependencyMap = new HashMap<>();
            StringBuilder inClauseBuilder = new StringBuilder();
            inClauseBuilder.append("(");
            while (rs.next()) {
                String sha1 = rs.getString(3);
                if (tempDependencyMap.get(sha1) == null) {
                    tempDependencyMap.put(sha1, sha1);
                    ModuleDependency dependency = new ModuleDependency(null, null, rs.getString(1),
                            rs.getString(2), rs.getString(4), sha1);
                    dependency.setStatus(rs.getString(5));
                    if (buildParams.isAllArtifact()) {
                        dependency.setModule(rs.getString(6));
                    }
                    dependencies.add(dependency);
                }
                inClauseBuilder.append("'" + sha1 + "'").append(",");
            }
            String inClause = inClauseBuilder.toString();
            inClause = inClause.substring(0, inClause.length() - 1);
            inClause = inClause + ")";
            // update dependencies repo path data
            if (!dependencies.isEmpty()) {
                rsDep = getModuleDependencyNodes(moduleDependencyMap, inClause);
                if (buildParams.isAllArtifact()) {
                    rsDepCompared = getModuleDependencyNodes(moduleDependencyMap, inClause);
                }
                dependencies.forEach(dependency -> {
                    ModuleDependency moduleDependency = moduleDependencyMap.get(dependency.getSha1());
                    if (moduleDependency != null) {
                        dependency.setRepoKey(moduleDependency.getRepoKey());
                        String path = moduleDependency.getPath();
                        String name = moduleDependency.getName();
                        if (path != null) {
                            dependency.setPath(path.equals(".") ? name : path + "/" + name);
                        }
                    }
                });
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rsDep);
            DbUtils.close(rsDepCompared);
            DbUtils.close(rs);
        }
        return dependencies;
    }

    /**
     * update build with specific condition for exclude and full build diff
     *
     * @param buildParams - build params
     * @param builder - build diff query writer
     */
    private void updateQueryWithSpecificConditions(BuildParams buildParams, StringBuilder builder) {
        if (buildParams.isExcludeInternalDependencies()) {
            // exclude internal dependencies
            builder.append(" where c not in (select build_modules.module_name_id  from build_modules \n" +
                    "inner join builds on builds.build_id = build_modules.build_id\n" +
                    " where builds.build_number=? and builds.build_date=?)");
        }
    }

    /**
     * get build dependency query params for all build dependency diff or build module dependency diff
     *
     * @param buildParams - build diff param
     * @return - build dependency param for diff query
     */
    private Object[] getBuildDependencyParams(BuildParams buildParams) {
        // build params for all build artifact query
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};
        } else {// build params for module build artifact query
            if (buildParams.isExcludeInternalDependencies()) {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()};
            } else {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
            }
        }
    }

    /**
     * get build dependency query for all build dependency diff or build module dependency diff
     *
     * @param buildParams - build diff param
     * @return - build dependency query for diff query
     */
    private String getBaseDependencyQuery(BuildParams buildParams) {
        String baseQuery;
        if (!buildParams.isAllArtifact()) {
            baseQuery = BuildQueries.MODULE_DEPENDENCY_DIFF_QUERY;
        } else {
            baseQuery = BuildQueries.BUILD_DEPENDENCY_DIFF_QUERY;
        }
        return baseQuery;
    }

    /**
     * get Module Artifact diff total Count
     *
     * @param offset - row offset
     * @param limit  - row limit
     * @return
     */
    public int getModuleDependencyForDiffCount(BuildParams buildParams, String offset, String limit) {
        ResultSet rs = null;
        String baseQuery;
        try {
            baseQuery = getBuildDependencyCountQuery(buildParams);
            Object[] diffParams = getBuildDependencyCountParam(buildParams);
            StringBuilder builder = new StringBuilder(baseQuery);
            if (buildParams.isExcludeInternalDependencies()) {
                // exclude internal dependencies
                builder.append("where dependency_name_id not in (select build_modules.module_name_id  from build_modules \n" +
                        "inner join builds on builds.build_id = build_modules.build_id\n" +
                        " where builds.build_number=? and builds.build_date=?)");
            }
            String buildQuery = builder.toString();
            rs = jdbcHelper.executeSelect(buildQuery, diffParams);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get Build Artifact Diff count param array (for module artifact / all artifact)
     *
     * @param buildParams - build diff param for query
     * @return - param array for diff
     */
    private Object[] getBuildDependencyCountParam(BuildParams buildParams) {
        if (!buildParams.isAllArtifact()) {
            return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(), buildParams.getBuildModuleId(),
                    buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(), buildParams.getBuildModuleId()};
        } else {
            if (buildParams.isExcludeInternalDependencies()) {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate()};
            } else {
                return new Object[]{buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getCurrBuildNum(), buildParams.getCurrBuildDate(),
                        buildParams.getComperedBuildNum(), buildParams.getComperedBuildDate()};
            }
        }
    }

    /**
     * get Build Artifact Diff count query (for module artifact / all artifact)
     *
     * @param buildParams - build diff param for query
     * @return - query array for diff
     */
    private String getBuildDependencyCountQuery(BuildParams buildParams) {
        String baseQuery;
        if (!buildParams.isAllArtifact()) {
            baseQuery = BuildQueries.MODULE_DEPENDENCY_DIFF_COUNT;
        } else {
            baseQuery = BuildQueries.BUILD_DEPENDENCY_DIFF_COUNT;
        }
        return baseQuery;
    }

    /**
     * get total build counts
     *
     * @param buildName - build name
     * @return
     * @throws SQLException
     */
    public int getBuildForNameTotalCount(String buildName) throws SQLException {
        ResultSet rs = null;
        try {
            QueryWriter queryWriter = new QueryWriter();
            String buildsQuery = queryWriter.select(" count(*) ").from(" builds ").where(" build_name = ? ").build();
            rs = jdbcHelper.executeSelect(buildsQuery, buildName);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    public long findBuildId(String name, String number, long startDate) throws SQLException {
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? AND build_number = ? AND build_date = ?",
                    name, number, startDate);
            if (rs.next()) {
                return rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return 0L;
    }

    public BuildEntity findBuild(String name, String number, long startDate) throws SQLException {
        long buildId = findBuildId(name, number, startDate);
        if (buildId > 0L) {
            return getBuild(buildId);
        }
        return null;
    }

    public BuildEntity getLatestBuild(String buildName) throws SQLException {
        long latestBuildDate = 0L;
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT max(build_date) FROM builds WHERE build_name = ?", buildName);
            if (rs.next()) {
                latestBuildDate = rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
            rs = null;
        }
        BuildEntity buildEntity = null;
        if (latestBuildDate > 0L) {
            try {
                rs = jdbcHelper.executeSelect("SELECT * FROM builds " +
                        "WHERE build_name = ? AND build_date = ?", buildName, latestBuildDate);
                if (rs.next()) {
                    buildEntity = resultSetToBuild(rs);
                }
            } finally {
                DbUtils.close(rs);
            }
        }
        if (buildEntity != null) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return buildEntity;
    }

    public long findLatestBuildDate(String buildName, String buildNumber) throws SQLException {
        long latestBuildDate = 0L;
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT max(build_date) FROM builds WHERE" +
                    " build_name = ? AND build_number = ?",
                    buildName, buildNumber);
            if (rs.next()) {
                latestBuildDate = rs.getLong(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return latestBuildDate;
    }

    public List<Long> findBuildIds(String buildName) throws SQLException {
        ResultSet rs = null;
        List<Long> buildIds = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? ORDER BY build_date DESC",
                    buildName);
            while (rs.next()) {
                buildIds.add(rs.getLong(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildIds;
    }

    public List<Long> findBuildIds(String buildName, String buildNumber) throws SQLException {
        ResultSet rs = null;
        List<Long> buildIds = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT build_id FROM builds WHERE" +
                    " build_name = ? AND build_number = ? ORDER BY build_date DESC",
                    buildName, buildNumber);
            while (rs.next()) {
                buildIds.add(rs.getLong(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildIds;
    }

    public List<String> getAllBuildNames() throws SQLException {
        ResultSet rs = null;
        List<String> buildNames = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(
                    "SELECT build_name, max(build_date) d FROM builds GROUP BY build_name ORDER BY d");
            while (rs.next()) {
                buildNames.add(rs.getString(1));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildNames;
    }

    /**
     * get build modules with paging
     * @param buildName - build name
     * @param date - date
     * @param orderBy - order by
     * @param direction - direction
     * @param offset - offset
     * @param limit - limit
     * @return
     * @throws SQLException
     */
    public List<PublishedModule> getBuildModule(String buildName, String date, String orderBy, String direction, String offset, String limit) throws SQLException {
        ResultSet rs = null;
        String buildQuery = "SELECT build_modules.module_name_id,\n" +
                "(select count(*) from build_artifacts where build_artifacts.module_id =  build_modules.module_id ) as num_of_art ,\n" +
                "(select count(*) from build_dependencies where build_dependencies.module_id =  build_modules.module_id ) as num_of_dep FROM build_modules\n" +
                "left join builds on builds.build_id=build_modules.build_id \n" +
                "where  builds.build_number=? and builds.build_date=?";

        List<PublishedModule> modules = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildName, Long.parseLong(date));
            while (rs.next()) {
                PublishedModule module = new PublishedModule();
                module.setId(rs.getString(1));
                module.setNumOfArtifact(rs.getString(2));
                module.setNumOfDependencies(rs.getString(3));
                modules.add(module);
            }
        } finally {
            DbUtils.close(rs);
        }
        return modules;
    }


    /**
     * get build modules artifact with paging
     *
     * @param buildNumber - build name
     * @param date        - build date
     * @param orderBy     - query order by
     * @param direction   - direction (asc /desc)
     * @param offset      - offset - start row
     * @param limit       - limit - end row
     * @return
     * @throws SQLException
     */
    public List<ModuleArtifact> getModuleArtifact(String buildName, String buildNumber, String date,
                                                  String moduleId, String orderBy, String direction, String offset,
                                                  String limit) throws SQLException {
        ResultSet rsArtifact = null;
        ResultSet rs = null;
        List<ModuleArtifact> artifacts = new ArrayList<>();
        Map<String, ModuleArtifact> artifactMap = new HashMap<>();
        try {
            // get artifact info
            rs = getPaginatedArtifact(buildNumber, Long.parseLong(date), moduleId, orderBy, direction, offset, limit);
            while (rs.next()) {
                artifacts.add(new ModuleArtifact(null, null, rs.getString(1), rs.getString(2), rs.getString(3)));
            }
            if (!artifacts.isEmpty()) {
                // query for artifact nodes
                rsArtifact = getArtifactNodes(buildName, buildNumber, artifactMap);
                for (ModuleArtifact artifact : artifacts) {
                    ModuleArtifact moduleArtifact = artifactMap.get(artifact.getSha1());
                    if (moduleArtifact != null) {
                        artifact.setRepoKey(moduleArtifact.getRepoKey());
                        artifact.setPath(moduleArtifact.getPath());
                    }
                }
            }

        } finally {
            DbUtils.close(rsArtifact);
            DbUtils.close(rs);
        }
        return artifacts;
    }

    /**
     * get module artifact info
     *
     * @param buildNumber - build number
     * @param date        - build date
     * @param moduleId    - module id
     * @param orderBy     - order by
     * @param direction   - direction
     * @param offset      - offset
     * @param limit       - limit
     * @return query result set
     */
    private ResultSet getPaginatedArtifact(String buildNumber, Long date,
                                           String moduleId, String orderBy, String direction, String offset,
                                           String limit) throws SQLException {
        ResultSet rs;
        String buildQuery = "SELECT distinct build_artifacts.artifact_name as name,build_artifacts.artifact_type as type,build_artifacts.sha1 FROM build_artifacts\n" +
                "left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        rs = jdbcHelper.executeSelect(buildQuery, buildNumber, date, moduleId);

        return rs;
    }

    /**
     * get Artifact nodes data by build name and number
     * Note - who ever use this method must be responsible for close the result set
     *
     * @param buildName   - build name
     * @param buildNumber - build number
     * @param artifactMap - map of data , key = sha1 , value = module artifact
     * @return query result set
     * @throws SQLException
     */
    private ResultSet getArtifactNodes(String buildName, String buildNumber, Map<String, ModuleArtifact> artifactMap) throws SQLException {
        ResultSet rsArtifact;
        rsArtifact = jdbcHelper.executeSelect("select distinct  n.repo,n.node_path,n.node_name,n.node_id,n.depth,n.sha1_actual,n.sha1_original,n.md5_actual,n.md5_original  \n" +
                "from  nodes n left outer join node_props np100 on np100.node_id = n.node_id left outer join node_props np101 on np101.node_id = n.node_id \n" +
                "where (( np100.prop_key = 'build.name' and  np100.prop_value = ?) and( np101.prop_key = 'build.number' and  np101.prop_value =?)) and n.node_type = 1", buildName, buildNumber);

        while (rsArtifact.next()) {
            String sha1 = rsArtifact.getString(6);
            if (artifactMap.get(sha1) == null) {
                artifactMap.put(sha1, new ModuleArtifact(rsArtifact.getString(1), rsArtifact.getString(2), rsArtifact.getString(3), null, null));
            }
        }
        return rsArtifact;
    }

    /**
     * get build modules dependencies with paging
     *
     * @param buildNumber - build name
     * @param date        - date
     * @param orderBy     - order by
     * @param direction   - direction
     * @param offset      - offset
     * @param limit       - limit
     * @return
     * @throws SQLException
     */
    public List<ModuleDependency> getModuleDependency(String buildNumber, String date, String moduleId, String orderBy,
                                                      String direction, String offset, String limit) throws SQLException {
        ResultSet rs = null;
        ResultSet rsDep = null;
        Map<String, ModuleDependency> moduleDependencyMap = new HashMap<>();
        String buildQuery = "SELECT distinct build_dependencies.dependency_name_id as id," +
                "build_dependencies.dependency_type as type,build_dependencies.dependency_scopes as scope," +
                "build_dependencies.sha1 FROM build_dependencies\n" +
                "left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        List<ModuleDependency> dependencies = new ArrayList<>();
        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildNumber, Long.parseLong(date), moduleId);
            StringBuilder inClauseBuilder = new StringBuilder();
            inClauseBuilder.append("(");
            while (rs.next()) {
                String sha1 = rs.getString(4);
                dependencies.add(new ModuleDependency(null, null, rs.getString(1), rs.getString(2), rs.getString(3),
                        sha1));
                inClauseBuilder.append("'" + sha1 + "'").append(",");
            }
            String inClause = inClauseBuilder.toString();
            inClause = inClause.substring(0, inClause.length() - 1);
            inClause = inClause + ")";

            if (!dependencies.isEmpty()) {
                // get repo key and path data for dependency
                rsDep = getModuleDependencyNodes(moduleDependencyMap, inClause);
                dependencies.forEach(dependency -> {
                    ModuleDependency moduleDependency = moduleDependencyMap.get(dependency.getSha1());
                    if (moduleDependency != null) {
                        dependency.setRepoKey(moduleDependency.getRepoKey());
                        String path = moduleDependency.getPath();
                        String name = moduleDependency.getName();
                        if (path != null) {
                            dependency.setPath(path.equals(".") ? name : path + "/" + name);
                        }
                    }
                });
            }
        } finally {
            DbUtils.close(rsDep);
            DbUtils.close(rs);
        }
        return dependencies;
    }

    private ResultSet getModuleDependencyNodes(Map<String, ModuleDependency> moduleDependencyMap, String inClause)
            throws SQLException {
        ResultSet rsDep = jdbcHelper.executeSelect(
                "SELECT distinct nodes.repo,nodes.node_path,nodes.node_name,nodes.sha1_actual FROM nodes\n" +
                        "                where nodes.sha1_actual in " + inClause);
        while (rsDep.next()) {
            String sha1 = rsDep.getString(4);
            if (moduleDependencyMap.get(sha1) == null) {
                moduleDependencyMap.put(sha1, new ModuleDependency(rsDep.getString(1), rsDep.getString(2), rsDep.getString(3), null, null, null));
            }
        }
        return rsDep;
    }


    /**
     * get build modules dependencies count with paging
     *
     * @param buildNumber - build name
     * @param date        - date
     * @return
     * @throws SQLException
     */
    public int getModuleDependenciesCount(String buildNumber, String date, String moduleId) throws SQLException {
        ResultSet rs = null;
        String buildQuery = "SELECT count(*) FROM build_dependencies\n" +
                "left join build_modules on build_modules.module_id=build_dependencies.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildNumber, date, moduleId);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get build modules count with paging
     *
     * @param buildNumber - build name
     * @param date        - date
     * @return
     * @throws SQLException
     */
    public int getModuleArtifactCount(String buildNumber, String date, String moduleId) throws SQLException {
        ResultSet rs = null;
        String buildQuery = "SELECT count(*) FROM build_artifacts\n" +
                "left join build_modules on build_modules.module_id=build_artifacts.module_id\n" +
                "left join builds on  build_modules.build_id = builds.build_id\n" +
                "where builds.build_number = ? and builds.build_date = ? and build_modules.module_name_id = ?";

        try {
            rs = jdbcHelper.executeSelect(buildQuery, buildNumber, date, moduleId);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    /**
     * get total published modules count
     *
     * @param buildName - build name
     * @param date      - date
     * @return num of total published modules
     */
    public int  getPublishedModulesCounts(String buildName, String date) {
        ResultSet rs = null;
        String buildQuery = "SELECT count(*) as cnt FROM build_modules\n" +
                "left join builds on builds.build_id=build_modules.build_id \n" +
                "where  builds.build_number=? and builds.build_date=?";
         try {
            rs = jdbcHelper.executeSelect(buildQuery,buildName,date);
            if  (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return 0;
    }

    public List<BuildEntity> getAllBuildNamePaging(String offset, String orderBy, String direction, String limit) throws SQLException {
        ResultSet rs = null;
        List<BuildEntity> buildNames = new ArrayList<>();
        try {
            String allBuildsQuery = "select * from (\n" +
                    "  (SELECT build_name buildName , max(build_date) build_time\n" +
                    "   from builds c GROUP BY build_name ) build_a\n" +
                    "   inner join (select * from builds)  build_b on build_b.build_date = build_a.build_time) \n" +
                    "where build_a.buildName=build_b.build_name";
            rs = jdbcHelper.executeSelect(allBuildsQuery);
            BuildEntity buildEntity;
            while (rs.next()) {
                buildEntity = new BuildEntity(rs.getInt(3), rs.getString(1), rs.getString(5), rs.getLong(2), rs.getString(7), rs.getLong(8), rs.getString(9), 0, null);
                buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
                buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
                buildNames.add(buildEntity);
            }
        } catch (Exception e) {
            log.error(e.toString());
        } finally {
            DbUtils.close(rs);
        }
        return buildNames;
    }

    public Collection<BuildEntity> findBuildsForArtifactChecksum(ChecksumType type, String checksum) throws SQLException {
        Collection<BuildEntity> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT DISTINCT b.* FROM builds b, build_artifacts ba, build_modules bm" +
                    " WHERE b.build_id = bm.build_id" +
                    " AND bm.module_id = ba.module_id" +
                    " AND ba." + type.name() + " = ?" +
                    " AND ba.module_id = bm.module_id", checksum);
            while (rs.next()) {
                results.add(resultSetToBuild(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        for (BuildEntity buildEntity : results) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return results;
    }

    public Collection<BuildEntity> findBuildsForDependencyChecksum(ChecksumType type, String checksum) throws SQLException {
        Collection<BuildEntity> results = Lists.newArrayList();
        ResultSet rs = null;
        try {
            rs = jdbcHelper.executeSelect("SELECT DISTINCT b.* FROM builds b, build_dependencies bd, build_modules bm" +
                    " WHERE b.build_id = bm.build_id" +
                    " AND bm.module_id = bd.module_id" +
                    " AND bd." + type.name() + " = ?" +
                    " AND bd.module_id = bm.module_id", checksum);
            while (rs.next()) {
                results.add(resultSetToBuild(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        for (BuildEntity buildEntity : results) {
            buildEntity.setProperties(findBuildProperties(buildEntity.getBuildId()));
            buildEntity.setPromotions(findBuildPromotions(buildEntity.getBuildId()));
        }
        return results;
    }

    private Set<BuildProperty> findBuildProperties(long buildId) throws SQLException {
        ResultSet rs = null;
        Set<BuildProperty> buildProperties = new HashSet<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM build_props WHERE" +
                    " build_id = ?",
                    buildId);
            while (rs.next()) {
                buildProperties.add(resultSetToBuildProperty(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildProperties;
    }

    private SortedSet<BuildPromotionStatus> findBuildPromotions(long buildId) throws SQLException {
        ResultSet rs = null;
        SortedSet<BuildPromotionStatus> buildPromotions = new TreeSet<>();
        try {
            rs = jdbcHelper.executeSelect("SELECT * FROM build_promotions WHERE" +
                    " build_id = ?",
                    buildId);
            while (rs.next()) {
                buildPromotions.add(resultSetToBuildPromotion(rs));
            }
        } finally {
            DbUtils.close(rs);
        }
        return buildPromotions;
    }

    private BuildProperty resultSetToBuildProperty(ResultSet rs) throws SQLException {
        return new BuildProperty(rs.getLong(1), rs.getLong(2),
                rs.getString(3), rs.getString(4));
    }

    private BuildPromotionStatus resultSetToBuildPromotion(ResultSet rs) throws SQLException {
        return new BuildPromotionStatus(rs.getLong(1), rs.getLong(2),
                rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6),
                rs.getString(7));
    }

    private BuildEntity resultSetToBuild(ResultSet rs) throws SQLException {
        return new BuildEntity(rs.getLong(1),
                rs.getString(2), rs.getString(3), rs.getLong(4),
                rs.getString(5), rs.getLong(6), rs.getString(7),
                zeroIfNull(rs.getLong(8)), rs.getString(9)
        );
    }

    private GeneralBuild resultSetToGeneralBuild(ResultSet rs, Long id) throws SQLException {
        return new GeneralBuild(id,
                rs.getString(2), rs.getString(3), rs.getLong(4),
                rs.getString(5), rs.getLong(6), rs.getString(7),
                zeroIfNull(rs.getLong(8)), rs.getString(9)
        );
    }
}
