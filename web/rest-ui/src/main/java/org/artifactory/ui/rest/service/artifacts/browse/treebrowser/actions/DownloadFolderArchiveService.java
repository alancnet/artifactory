package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.archive.ArchiveType;
import org.artifactory.api.common.BasicStatusHolder;
import org.artifactory.api.download.FolderDownloadService;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.RepoPathFactory;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.action.DownloadFolder;
import org.artifactory.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;

/**
 * Downloads a folder in the requested format.
 *
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DownloadFolderArchiveService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DownloadFolderArchiveService.class);

    @Autowired
    FolderDownloadService folderDownloadService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DownloadFolder folderToDownload = new DownloadFolder(request);
        String repoKey = folderToDownload.getRepoKey();
        String path = folderToDownload.getPath();
        RepoPath pathToDownload = RepoPathFactory.create(repoKey, path);
        respondWithArchiveStream((StreamRestResponse) response, folderToDownload.getArchiveType(), pathToDownload);
    }

    private void respondWithArchiveStream(StreamRestResponse response, ArchiveType archiveType, RepoPath folder) {
        BasicStatusHolder status = new BasicStatusHolder();
        //Defaults to zip
        archiveType = archiveType != null ? archiveType : ArchiveType.ZIP;
        InputStream is = folderDownloadService.process(folder, archiveType, status);
        if (status.isError()) {
            response.error(status.getLastError().getMessage()).responseCode(status.getLastError().getStatusCode());
        } else if (is == null) {
            response.error("Unexpected error encountered, download stream is null - check the log for additional info");
        } else {
            response.setDownloadFile(createArchiveFileName(folder, archiveType));
            response.setDownload(true);
            response.iModel((StreamingOutput) out -> {
                try {
                    IOUtils.copy(is, out);
                } finally {
                    log.debug("Closing folder download stream.");
                    out.flush();
                    IOUtils.closeQuietly(is);
                    IOUtils.closeQuietly(out);
                }
            });
        }
    }

    /**
     * <Folder name>.<archive extension>
     */
    private String createArchiveFileName(RepoPath path, ArchiveType archiveType) {
        return (path.isRoot() ? path.getRepoKey()
                : PathUtils.getLastPathElement(path.getPath())) + "." + archiveType.value();
    }
}
