package org.artifactory.ui.rest.model.setmeup;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.model.BaseModel;

import java.util.Set;

/**
 * @author chen keinan
 */
public class MavenSettingModel extends BaseModel {

    private String release;
    private String snapshot;
    private String pluginRelease;
    private String pluginSnapshot;
    private String mirror;
    private String mavenSnippet;
    private Set<String> releases = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "release") && !StringUtils.containsIgnoreCase(o1, "plugin") ?
                    -1 : 1);
    private Set<String> pluginReleases = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "plugin") && StringUtils.containsIgnoreCase(o1, "release") ?
                    -1 : 1);
    private Set<String> snapshots = Sets.newTreeSet((o1, o2) ->
            StringUtils.containsIgnoreCase(o1, "snapshot") && !StringUtils.containsIgnoreCase(o1, "plugin") ? -1 : 1);
    private Set<String> pluginSnapshots = Sets.newTreeSet(
            (o1, o2) -> StringUtils.containsIgnoreCase(o1, "plugin") && StringUtils.containsIgnoreCase(o1, "snapshot") ?
                    -1 : 1);
    private Set<String> anyMirror = Sets.newTreeSet((o1, o2) -> StringUtils.containsIgnoreCase(o1, "release") ? 1 : -1);

    public MavenSettingModel(){}

    public MavenSettingModel (String mavenSnippet){
        this.mavenSnippet = mavenSnippet;
    }

    public Set<String> getReleases() {
        return releases;
    }

    public void setReleases(Set<String> releases) {
        this.releases = releases;
    }

    public Set<String> getPluginReleases() {
        return pluginReleases;
    }

    public void setPluginReleases(Set<String> pluginReleases) {
        this.pluginReleases = pluginReleases;
    }

    public Set<String> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Set<String> snapshots) {
        this.snapshots = snapshots;
    }

    public Set<String> getPluginSnapshots() {
        return pluginSnapshots;
    }

    public void setPluginSnapshots(Set<String> pluginSnapshots) {
        this.pluginSnapshots = pluginSnapshots;
    }

    public Set<String> getAnyMirror() {
        return anyMirror;
    }

    public void setAnyMirror(Set<String> anyMirror) {
        this.anyMirror = anyMirror;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(String snapshot) {
        this.snapshot = snapshot;
    }

    public String getPluginRelease() {
        return pluginRelease;
    }

    public void setPluginRelease(String pluginRelease) {
        this.pluginRelease = pluginRelease;
    }

    public String getPluginSnapshot() {
        return pluginSnapshot;
    }

    public void setPluginSnapshot(String pluginSnapshot) {
        this.pluginSnapshot = pluginSnapshot;
    }

    public String getMirror() {
        return mirror;
    }

    public void setMirror(String mirror) {
        this.mirror = mirror;
    }

    public String getMavenSnippet() {
        return mavenSnippet;
    }

    public void setMavenSnippet(String mavenSnippet) {
        this.mavenSnippet = mavenSnippet;
    }

    public void clearProps(){
        releases = null;
        pluginReleases = null;
        snapshots = null;
        pluginSnapshots = null;
        anyMirror = null;
    }
}
