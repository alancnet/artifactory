package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import org.apache.commons.io.IOUtils;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.FileInfo;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.DockerArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.DockerConfig;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.DockerInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.DockerMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerViewService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DockerViewService.class);

    @Autowired
    RepositoryService repositoryService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        DockerArtifactInfo dockerArtifactInfo = (DockerArtifactInfo) request.getImodel();
        String path = dockerArtifactInfo.getPath();
        String repoKey = dockerArtifactInfo.getRepoKey();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        FileInfo fileInfo = repositoryService.getFileInfo(repoPath);
        DockerMetadata metadata = extractMetadata(fileInfo, response);
        dockerArtifactInfo.clearRepoData();
        String basePath = path.split("/")[0];
        // get docker info
        DockerInfo dockerInfo = getDockerInfo(metadata, basePath);
        dockerArtifactInfo.setDockerInfo(dockerInfo);
        // get docker config
        DockerConfig dockerConfig = getDockerConfig(metadata);
        dockerArtifactInfo.setDockerConfig(dockerConfig);
        response.iModel(dockerArtifactInfo);
    }

    /**
     * populate data from docker metadata file to docker metadata model
     *
     * @param metadata - docker meta
     * @return docker meta data model
     */
    private DockerConfig getDockerConfig(DockerMetadata metadata) {
        DockerConfig dockerConfig = null;
        if (metadata.config != null) {
            dockerConfig = new DockerConfig();
            dockerConfig.setHostname(metadata.config.hostname);
            dockerConfig.setDomainName(metadata.config.domainname);
            dockerConfig.setUser(metadata.config.user);
            dockerConfig.setMemory(getJsonValue(String.valueOf(metadata.config.memory)));
            dockerConfig.setMemorySwap(getJsonValue(String.valueOf(metadata.config.memorySwap)));
            dockerConfig.setCpuShares(getJsonValue(String.valueOf(metadata.config.cpuShares)));
            dockerConfig.setCpuSet(metadata.config.cpuSet);
            dockerConfig.setAttachStdin(getJsonValue(String.valueOf(metadata.config.attachStdin)));
            dockerConfig.setAttachStdout(metadata.config.attachStdout);
            dockerConfig.setAttachStderr(getJsonValue(String.valueOf(metadata.config.attachStderr)));
            dockerConfig.setPortSpecs(getJsonValue(String.valueOf(metadata.config.portSpecs)));
            dockerConfig.setExposedPorts(getJsonValue(String.valueOf(metadata.config.exposedPorts)));
            dockerConfig.setTty(getJsonValue(String.valueOf(metadata.config.tty)));
            dockerConfig.setOpenStdin(getJsonValue(String.valueOf(metadata.config.openStdin)));
            dockerConfig.setStdinOnce(getJsonValue(String.valueOf(metadata.config.stdinOnce)));
            dockerConfig.setEnv(getJsonValue(String.valueOf(metadata.config.env)));
            dockerConfig.setCmd(getJsonValue(String.valueOf(metadata.config.cmd)));
            dockerConfig.setImage(metadata.config.image);
            dockerConfig.setVolumes(getJsonValue(String.valueOf(metadata.config.volumes)));
            dockerConfig.setWorkingDir(metadata.config.workingDir);
            dockerConfig.setEntryPoint(metadata.config.entrypoint);
            dockerConfig.setNetworkDisabled(getJsonValue(String.valueOf(metadata.config.networkDisabled)));
            dockerConfig.setOnBuild(getJsonValue(String.valueOf(metadata.config.onBuild)));
        }
        return dockerConfig;
    }

    private String getJsonValue(String value) {
        if (value == null || value.length() == 0 || value.equals("null")) {
            return null;
        }
        return value;
    }

    /**
     * get docker metadata info from doccker file
     *
     * @param metadata - docker metadata
     * @return docker info
     */
    private DockerInfo getDockerInfo(DockerMetadata metadata, String basePath) {
        DockerInfo dockerInfo = new DockerInfo();
        dockerInfo.setImageId(metadata.id);
        dockerInfo.setImageIdPath(basePath + "/" + metadata.id.substring(0, 2) + "/" + metadata.id);
        dockerInfo.setParent(metadata.parent);
        if (metadata.parent != null && metadata.parent.length() > 0) {
            dockerInfo.setParentIdPath(basePath + "/" + metadata.parent.substring(0, 2) + "/" + metadata.parent);
        }
        dockerInfo.setCreated(metadata.created);
        dockerInfo.setSize(metadata.size + " bits (" + humanReadableByteCount(metadata.size, true) + ")");
        dockerInfo.setContainer(metadata.container);
        dockerInfo.setDockerVersion(metadata.dockerVersion);
        dockerInfo.setArchitecture(metadata.architecture);
        dockerInfo.setOs(metadata.os);
        dockerInfo.setAuthor(metadata.author);
        return dockerInfo;
    }


    /**
     * extract docker metadata from docker file
     *
     * @param fileInfo            - docker file info
     * @param artifactoryResponse - encapsulate data require to response
     * @return - docker metadata Model
     */
    private DockerMetadata extractMetadata(FileInfo fileInfo, RestResponse artifactoryResponse) {
        ResourceStreamHandle handle = null;
        try {
            handle = repositoryService.getResourceStreamHandle(fileInfo.getRepoPath());
            return JacksonReader.streamAsClass(handle.getInputStream(), DockerMetadata.class);
        } catch (IOException e) {
            artifactoryResponse.error("Unable to extract Docker metadata for '" + fileInfo);
            log.error("Unable to extract Docker metadata for '" + fileInfo + "'", e);
        } finally {
            IOUtils.closeQuietly(handle);
        }
        return null;
    }

    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " bit";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sbit", bytes / Math.pow(unit, exp), pre);
    }
}
