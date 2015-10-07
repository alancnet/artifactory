package org.artifactory.aql.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.aql.AqlService;
import org.artifactory.aql.api.domain.sensitive.AqlApiItem;
import org.artifactory.aql.api.internal.AqlBase;
import org.artifactory.aql.result.AqlEagerResult;
import org.artifactory.aql.result.rows.AqlBaseFullRowImpl;
import org.artifactory.aql.result.rows.AqlItem;
import org.artifactory.aql.result.rows.AqlProperty;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.artifactory.aql.api.internal.AqlBase.and;

/**
 * General class for aql utilities
 *
 * @author Dan Feldman
 */
public class AqlUtils {
    private static final Logger log = LoggerFactory.getLogger(AqlUtils.class);

    public static final String COLUMN_REPO = "repo";
    public static final String COLUMN_PATH = "node_path";
    public static final String COLUMN_NAME = "node_name";
    public static final String COLUMN_SIZE = "bin_length";

    /**
     * Returns a RepoPath from an aql result's path fields
     *
     * @param repo repo key
     * @param path path
     * @param name file name
     */
    public static RepoPath fromAql(String repo, String path, String name) {
        if (StringUtils.equals(path, ".")) {
            return RepoPathFactory.create(repo, name);
        } else {
            return RepoPathFactory.create(repo, path + "/" + name);
        }
    }

    /**
     * Returns the RepoPath that points to this aql result
     *
     * @param row AqlFullRow object that has repo, path and name fields
     */
    public static RepoPath fromAql(AqlBaseFullRowImpl row) throws IllegalArgumentException {
        if (StringUtils.isBlank(row.getRepo()) || StringUtils.isBlank(row.getPath())
                || StringUtils.isBlank(row.getName())) {
            throw new IllegalArgumentException("Repo, Path, and Name fields must contain values");
        }
        return fromAql(row.getRepo(), row.getPath(), row.getName());
    }

    public static RepoPath fromAql(AqlItem item) {
        return fromAql((AqlBaseFullRowImpl) item);
    }

    /**
     * Returns true if the node that the path points to exists (Use with files only!)
     *
     * @param path repo path to check for existence
     */
    public static boolean exists(RepoPath path) {
        AqlSearchablePath aqlPath = new AqlSearchablePath(path);
        AqlApiItem aql = AqlApiItem.create().filter(
                and(
                        AqlApiItem.repo().equal(aqlPath.getRepo()),
                        AqlApiItem.path().equal(aqlPath.getPath()),
                        AqlApiItem.name().equal(aqlPath.getFileName())
                )
        );
        AqlEagerResult<AqlItem> results = ContextHelper.get().beanForType(AqlService.class).executeQueryEager(aql);
        return results != null && results.getResults() != null && results.getResults().size() > 0;
    }

    /**
     * Returns a list of {@link org.artifactory.aql.util.AqlSearchablePath} pointing to all files contained in the
     * current folder as well as all files under all subdirectories of that folder.
     *
     * NOTE: use only with folders!
     * @param path RepoPath of the folder to construct the search paths from
     */
    public static List<AqlSearchablePath> getSearchablePathForCurrentFolderAndSubfolders(RepoPath path) {
        List<AqlSearchablePath> artifactPaths = Lists.newArrayList();
        //Add *.* in filename for AqlSearchablePath creation - path is assumed to be a folder
        RepoPath searchPath = InternalRepoPathFactory.childRepoPath(path, "*.*");
        //All files in the folder containing the file
        AqlSearchablePath allFilesInCurrentFolder = new AqlSearchablePath(searchPath);
        //All files in all subfolders of folder containing the file
        AqlSearchablePath allFilesInSubFolders = new AqlSearchablePath(searchPath);
        if (".".equals(allFilesInSubFolders.getPath())) {  //Special case for root folder
            allFilesInSubFolders.setPath("**");
        } else {
            allFilesInSubFolders.setPath(allFilesInSubFolders.getPath() + "/**");
        }
        //This will also find files without any extension (i.e. docker, lfs)
        allFilesInCurrentFolder.setFileName("*");
        allFilesInSubFolders.setFileName("*");
        artifactPaths.add(allFilesInCurrentFolder);
        artifactPaths.add(allFilesInSubFolders);
        return artifactPaths;
    }

    /**
     * Returns an AqlApiItem OR clause containing an AND for each of the searchable paths given
     */
    public static AqlBase.OrClause getSearchClauseForPaths(List<AqlSearchablePath> aqlSearchablePaths) {
        AqlBase.OrClause searchClause = AqlBase.or();
        for (AqlSearchablePath path : aqlSearchablePaths) {
            log.debug("Adding path '{}' to artifact search", path.toRepoPath().toString());
            searchClause.append(
                    and(
                            AqlApiItem.repo().matches(path.getRepo()),
                            AqlApiItem.path().matches(path.getPath()),
                            AqlApiItem.name().matches(path.getFileName())
                    )
            );
        }
        return searchClause;
    }

    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.trace("Could not close JDBC result set", e);
            } catch (Exception e) {
                log.trace("Unexpected exception when closing JDBC result set", e);
            }
        }
    }

    public static RepoPath pathFromLazyRow(ResultSet resultSet) throws SQLException {
        String repo = resultSet.getString(AqlUtils.COLUMN_REPO);
        String path = resultSet.getString(AqlUtils.COLUMN_PATH);
        String name = resultSet.getString(AqlUtils.COLUMN_NAME);
        if (StringUtils.isBlank(repo) || StringUtils.isBlank(path) || StringUtils.isBlank(name)) {
            log.debug("Got bad item info from query row: repo: {}, path: {}, name: {}, size: {}", repo, path, name);
            return null;
        }
        return fromAql(repo, path, name);
    }

    /**
     * Returns a multimap of propertyKey -> values
     *
     * @param results The AQL properties search result
     */
    public static HashMultimap<String, String> propsToMap(AqlEagerResult<AqlProperty> results) {
        HashMultimap<String, String> map = HashMultimap.create();
        for (AqlProperty property : results.getResults()) {
            map.put(property.getKey(), property.getValue());
        }
        return map;
    }
}
