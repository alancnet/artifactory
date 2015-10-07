package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.distributionmngt;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.repo.LocalCacheRepoDescriptor;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RemoteRepoDescriptor;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.util.HttpUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties({"repoKey", "path","httpServletRequest","firstIndention","secondIndention","isCache"})
public class DistributionManagement extends BaseModel {

    private String firstIndention = "";
    private String secondIndention = "    ";
    private boolean isCache = false;
    private String distributedManagement;


    public String getDistributedManagement() {
        return distributedManagement;
    }

    public void setDistributedManagement(String distributedManagement) {
        this.distributedManagement = distributedManagement;
    }


    public StringBuilder populateDistributionManagement(LocalRepoDescriptor repo, CentralConfigService cc,
            HttpServletRequest httpServletRequest) {
         final StringBuilder sb = new StringBuilder();
        sb.delete(0, sb.length());
         String id = cc.getServerName();
        String repoUrl = buildRepoUrl(repo,httpServletRequest);
        isCache = repo.isCache();
        setIndentions(isCache);
        if (!isCache) {
            sb.append("<distributionManagement>\n");
        }
        if (repo.isHandleReleases()) {
            sb.append(firstIndention);
            sb.append("<repository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append(id);
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-releases</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</repository>\n");
        }

        if (repo.isHandleSnapshots()) {
            sb.append(firstIndention);
            sb.append("<snapshotRepository>\n");
            sb.append(secondIndention);
            sb.append("<id>");
            sb.append(id);
            sb.append("</id>\n");
            sb.append(secondIndention);
            sb.append("<name>");
            sb.append(id);
            sb.append("-snapshots</name>\n");
            sb.append(secondIndention);
            sb.append("<url>");
            sb.append(repoUrl);
            sb.append("</url>\n");
            sb.append(firstIndention);
            sb.append("</snapshotRepository>\n");
        }
        if (!isCache) {
            sb.append("</distributionManagement>");
        }
        return sb;
    }

    private String buildRepoUrl(LocalRepoDescriptor repo,HttpServletRequest httpServletRequest) {
        String servletContextUrl = HttpUtils.getServletContextUrl(httpServletRequest);
        if (!servletContextUrl.endsWith("/")) {
            servletContextUrl += "/";
        }
        StringBuilder sb = new StringBuilder();
        if (repo instanceof LocalCacheRepoDescriptor) {
            RemoteRepoDescriptor remoteRepoDescriptor = ((LocalCacheRepoDescriptor) repo).getRemoteRepo();
            if (remoteRepoDescriptor != null) {
                sb.append(servletContextUrl).append(remoteRepoDescriptor.getKey());
            } else {
                String fixedKey = StringUtils.remove(repo.getKey(), "-cache");
                sb.append(servletContextUrl).append(fixedKey);
            }
        } else {
            sb.append(servletContextUrl).append(repo.getKey());
        }
        return sb.toString();
    }
    private void setIndentions(boolean isCache) {
        if (!isCache) {
            firstIndention += "    ";
            secondIndention += "    ";
        }
    }
}
