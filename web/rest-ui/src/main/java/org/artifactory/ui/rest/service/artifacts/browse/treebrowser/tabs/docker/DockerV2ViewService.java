package org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.jackson.JacksonReader;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.fs.ItemInfo;
import org.artifactory.md.Properties;
import org.artifactory.repo.InternalRepoPathFactory;
import org.artifactory.repo.RepoPath;
import org.artifactory.resource.ResourceStreamHandle;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.BaseArtifactInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2.DockerBlobInfo;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2.DockerImageMetadata;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2.DockerUnits;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.docker.v2.DockerV2Info;
import org.codehaus.jackson.JsonNode;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Shay Yaakov
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DockerV2ViewService implements RestService {
    private static final Logger log = LoggerFactory.getLogger(DockerV2ViewService.class);

    @Autowired
    RepositoryService repoService;

    @Autowired
    CentralConfigService configService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        BaseArtifactInfo artifactInfo = (BaseArtifactInfo) request.getImodel();
        String path = artifactInfo.getPath();
        String repoKey = artifactInfo.getRepoKey();
        RepoPath repoPath = InternalRepoPathFactory.create(repoKey, path);
        ItemInfo manifest = repoService.getChildren(repoPath)
                .stream().filter(itemInfo -> itemInfo.getName().equals("manifest.json"))
                .findFirst().orElseGet(null);

        if (manifest != null) {
            try (ResourceStreamHandle handle = repoService.getResourceStreamHandle(manifest.getRepoPath())) {
                DockerV2Info dockerV2Info = getDockerV2Info(handle, getDigest(manifest.getRepoPath()));
                response.iModel(dockerV2Info);
            } catch (IOException e) {
                response.error("Unable to extract Docker metadata for '" + repoPath);
                log.error("Unable to extract Docker metadata for '" + repoPath + "'", e);
            }
        } else {
            response.error("Unable to find Docker manifest under '" + repoPath);
            log.error("Unable to find Docker manifest under '" + repoPath + "'");
        }
    }

    private String getDigest(RepoPath repoPath) {
        Properties properties = repoService.getProperties(repoPath);
        return properties != null ? properties.getFirst("sha256") : null;
    }

    private DockerV2Info getDockerV2Info(ResourceStreamHandle handle, String digest) throws IOException {
        DockerV2Info dockerV2Info = new DockerV2Info();

        JsonNode manifest = JacksonReader.streamAsTree(handle.getInputStream());
        dockerV2Info.tagInfo.title = manifest.get("name").asText() + ":" + manifest.get("tag").asText();
        dockerV2Info.tagInfo.digest = digest;
        long totalSize = 0;
        JsonNode history = manifest.get("history");
        for (int i = 0; i < history.size(); i++) {
            JsonNode fsLayer = history.get(i);
            String v1Compatibility = fsLayer.get("v1Compatibility").asText();
            DockerImageMetadata dockerMetadata = JacksonReader.bytesAsClass(v1Compatibility.getBytes(),
                    DockerImageMetadata.class);

            String command = dockerMetadata.config.cmd.get(2);
            String blobDigest = manifest.get("fsLayers").get(i).get("blobSum").asText();
            totalSize += dockerMetadata.size;
            String size = DockerUnits.humanReadableByteCount(dockerMetadata.size, true);
            String created = configService.format(
                    ISODateTimeFormat.dateTimeParser().parseMillis(dockerMetadata.created));
            DockerBlobInfo blobInfo = new DockerBlobInfo(dockerMetadata.id, blobDigest, size, created);
            if (StringUtils.contains(command, "(nop)")) {
                command = StringUtils.substringAfter(command, "(nop) ");
                String dockerCmd = StringUtils.substringBefore(command, " ");
                command = StringUtils.substringAfter(command, " ");
                blobInfo.command = dockerCmd;
                blobInfo.commandText = command;
                dockerV2Info.blobsInfo.add(blobInfo);
            } else {
                // RUN command
                blobInfo.command = "RUN";
                blobInfo.commandText = command;
                dockerV2Info.blobsInfo.add(blobInfo);
            }

            JsonNode exposedPorts = dockerMetadata.config.exposedPorts;
            if (exposedPorts != null) {
                exposedPorts.getFieldNames().forEachRemaining(dockerV2Info.tagInfo.ports::add);
            }

            JsonNode volumes = dockerMetadata.config.volumes;
            if (volumes != null) {
                volumes.getFieldNames().forEachRemaining(dockerV2Info.tagInfo.volumes::add);
            }
        }
        dockerV2Info.tagInfo.totalSize = DockerUnits.humanReadableByteCount(totalSize, true);
        return dockerV2Info;
    }
}
