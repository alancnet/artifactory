package org.artifactory.rest.resource.archive;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.exception.NotFoundException;
import org.artifactory.rest.common.util.RestUtils;
import org.artifactory.search.archive.InternalArchiveIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.artifactory.api.rest.constant.ArchiveIndexRestConstants.PATH_ROOT;

/**
 * Resource class which handles archive indexing.
 *
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Path(PATH_ROOT)
@RolesAllowed(AuthorizationService.ROLE_ADMIN)
public class ArchiveIndexResource {
    private static final Logger log = LoggerFactory.getLogger(ArchiveIndexResource.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private InternalArchiveIndexer archiveIndexer;

    @POST
    @Path("{path: .+}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response index(@PathParam("path") String path) {
        RepoPath repoPath = RestUtils.calcRepoPathFromRequestPath(path);
        boolean allRepos = isAllRepos(repoPath.getRepoKey());
        if (!allRepos) {
            if (!repositoryService.exists(repoPath)) {
                throw new NotFoundException("Could not find repo path " + repoPath);
            }
        }

        archiveIndexer.recursiveMarkArchivesForIndexing(repoPath, allRepos);

        //trigger async archive indexing now
        archiveIndexer.asyncIndexMarkedArchives();

        String message = allRepos ? "Archive indexing of all repositories accepted." :
                "Archive indexing for path '" + repoPath + "' accepted.";
        log.info(message);
        return Response.status(HttpStatus.SC_ACCEPTED).entity(message).build();
    }

    private boolean isAllRepos(String repoKey) {
        return StringUtils.equals(repoKey, "*");
    }
}
