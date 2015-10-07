package org.artifactory.ui.rest.model.admin.configuration.ha;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.storage.db.servers.model.ArtifactoryServer;

/**
 * @author Chen Keinan
 */
public class HaModel {

    private String id;
    private String startTime;
    private String url;
    private int memberShipPort;
    private String state;
    private String role;
    private String lastHeartbeat;
    private String version;
    private int revision;
    private String releaseDate;
    private boolean isHeartbeatStale;

    public HaModel(ArtifactoryServer server, boolean isHeartbeatStale) {
        id = server.getServerId();
        CentralConfigService centralConfig = ContextHelper.get().getCentralConfig();
        startTime = centralConfig.getDateFormatter().print(server.getStartTime());
        url = server.getContextUrl();
        memberShipPort = server.getMembershipPort();
        state = server.getServerState().getPrettyName();
        role = server.getServerRole().getPrettyName();
        lastHeartbeat = centralConfig.getDateFormatter().print(server.getLastHeartbeat());
        version = server.getArtifactoryVersion();
        revision = server.getArtifactoryRevision();
        releaseDate = centralConfig.getDateFormatter().print(server.getArtifactoryRelease());
        this.isHeartbeatStale = isHeartbeatStale;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getMemberShipPort() {

        return memberShipPort;
    }

    public void setMemberShipPort(int memberShipPort) {
        this.memberShipPort = memberShipPort;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(String lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isHeartbeatStale() {
        return isHeartbeatStale;
    }

    public void setIsHeartbeatStale(boolean isHeartbeatStale) {
        this.isHeartbeatStale = isHeartbeatStale;
    }
}
