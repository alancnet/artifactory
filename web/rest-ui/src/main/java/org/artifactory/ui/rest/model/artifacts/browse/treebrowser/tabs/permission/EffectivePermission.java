package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.permission;

import org.artifactory.security.AceInfo;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties("hasAtLeastOnePermission")
public class EffectivePermission {

    private String principal;
    private boolean delete;
    private boolean deploy;
    private boolean annotate;
    private boolean read;
    private Boolean managed;
    private boolean hasAtLeastOnePermission;
    private Integer mask;

    public EffectivePermission() {
    }

    public EffectivePermission(AceInfo aceInfo) {
        principal = aceInfo.getPrincipal();
        delete = aceInfo.canDelete();
        deploy = aceInfo.canDeploy();
        annotate = aceInfo.canAnnotate();
        read = aceInfo.canRead();
        managed = aceInfo.canManage();
        mask = aceInfo.getMask();
    }

    public boolean isHasAtLeastOnePermission() {
        return hasAtLeastOnePermission;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
        if (delete){
            hasAtLeastOnePermission = true;
        }
    }

    public boolean isDeploy() {
        return deploy;
    }

    public void setDeploy(boolean deploy) {
        this.deploy = deploy;
        if (deploy){
            hasAtLeastOnePermission = true;
        }
    }

    public boolean isAnnotate() {
        return annotate;
    }

    public void setAnnotate(boolean annotate) {
        this.annotate = annotate;
        if (annotate){
            hasAtLeastOnePermission = true;
        }
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
        if (read){
            hasAtLeastOnePermission = true;
        }
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public Boolean isManaged() {
        return managed;
    }

    public void setManaged(Boolean managed) {
        this.managed = managed;
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }
}
