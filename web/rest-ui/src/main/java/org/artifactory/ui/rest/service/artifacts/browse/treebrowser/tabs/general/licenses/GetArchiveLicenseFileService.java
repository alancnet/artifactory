package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.ArchiveFileContent;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.common.ConstantValues;
import org.artifactory.fs.ZipEntryInfo;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.licenses.GeneralTabLicenseModel;
import org.artifactory.ui.utils.RequestUtils;
import org.artifactory.util.Tree;
import org.artifactory.util.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dan Feldman
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetArchiveLicenseFileService implements RestService<GeneralTabLicenseModel> {
    private static final Logger log = LoggerFactory.getLogger(GetArchiveLicenseFileService.class);

    @Autowired
    private RepositoryService repoService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        RepoPath path = RequestUtils.getPathFromRequest(request);
        MimeType mimeType = NamingUtils.getMimeType(path.getPath());
        if (!mimeType.isArchive()) {
            response.error("File is not an archive").responseCode(HttpStatus.SC_BAD_REQUEST);
            return;
        }
        getArchiveLicenseFileContent(path, response);
    }

    private void getArchiveLicenseFileContent(RepoPath path, RestResponse response) {
        try {
            Tree<ZipEntryInfo> zipEntries = repoService.zipEntriesToTree(path);
            Set<String> licenseFileNames = getPossibleLicenseFileNames();
            TreeNode<ZipEntryInfo> licenseEntry = searchForLicenseRecursively(zipEntries.getRoot(), licenseFileNames);
            if (licenseEntry != null) {
                String entryPath = licenseEntry.getData().getPath();
                final ArchiveFileContent archiveFileContent = repoService.getArchiveFileContent(path, entryPath);
                final String archiveContent = archiveFileContent.getContent();
                if (StringUtils.isBlank(archiveContent)) {
                    String err = "Unable to retrieve the content of the archived file";
                    log.warn("{} {}/{}: {}", err, path, entryPath, archiveFileContent.getFailureReason());
                    response.error(err + " : " + archiveFileContent.getFailureReason())
                            .responseCode(HttpStatus.SC_NOT_FOUND);
                } else {
                    response.iModel(archiveContent);
                }
            } else {
                response.warn("No license file found in archive").responseCode(HttpStatus.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            String err = "An error occurred while trying to locate or retrieve license file content of ";
            log.error("{} {}: {}", err, path, e.getMessage());
            log.debug("{} {}.", err, path, e);
            response.error(err + path + ": " + e.getMessage());
        }
    }

    private Set<String> getPossibleLicenseFileNames() {
        return Stream.of(StringUtils.split(ConstantValues.archiveLicenseFileNames.getString(), ","))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }

    private TreeNode<ZipEntryInfo> searchForLicenseRecursively(TreeNode<ZipEntryInfo> root,
                                                               Set<String> possibleLicenseFileNames) {
        if (root.hasChildren()) {
            for (TreeNode<ZipEntryInfo> zipEntryInfoTreeNode : root.getChildren()) {
                ZipEntryInfo data = zipEntryInfoTreeNode.getData();
                String name = data.getName();
                if (data.isDirectory()) {
                    TreeNode<ZipEntryInfo> licenseEntry = searchForLicenseRecursively(zipEntryInfoTreeNode,
                            possibleLicenseFileNames);
                    if (licenseEntry != null) {
                        return licenseEntry;
                    }
                } else if (possibleLicenseFileNames.contains(name)) {
                    return zipEntryInfoTreeNode;
                }
            }
        }
        return null;
    }
}
