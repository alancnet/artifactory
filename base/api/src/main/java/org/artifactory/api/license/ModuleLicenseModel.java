package org.artifactory.api.license;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.api.build.BuildService;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.api.security.AuthorizationService;
import org.artifactory.repo.RepoPath;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jfrog.build.api.Dependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.artifactory.api.license.LicenseInfo.*;

/**
 * @author Chen Keinan
 */
@JsonIgnoreProperties({"repoPath", "neutral", "scopes"})
public class ModuleLicenseModel {

    private String id;
    private String md5;
    private String sha1;
    private LicenseInfo license = NOT_FOUND_LICENSE;              //License set as prop
    private LicenseInfo extractedLicense = NOT_SEARCHED_LICENSE;  //Licenses from archive / descriptor
    private Set<String> scopes = Sets.newHashSet();
    private String scopeNames = "";
    private boolean selected;
    private boolean overridable = false;

    public void setOverridable(boolean overridable) {
        this.overridable = overridable;
    }

    private boolean hasConflicts = false;
    private boolean notFound = false;
    private boolean neutral = false;
    private RepoPath repoPath;
    private String repoKey;
    private String path;
    private String displayName;
    List<String> actions = new ArrayList<>();

    public ModuleLicenseModel() {
    }

    public ModuleLicenseModel(RepoPath repoPath) {
        this.repoPath = repoPath;
        if (repoPath != null) {
            addActions(repoPath);
        } else {
            repoKey = "";
            path = "No path found (externally resolved or deleted/overwritten)";
        }
        actions.add("EditLicense");
    }

    /**
     * add actions to artifact
     *
     * @param repoPath - artifact repo path
     */
    private void addActions(RepoPath repoPath) {
        AuthorizationService authService = ContextHelper.get().getAuthorizationService();
        this.repoKey = repoPath.getRepoKey();
        this.path = repoPath.getPath();
        actions.add("ChangeLicense");
        if (authService.canRead(repoPath)) {
            actions.add("ShowInTree");
            actions.add("Download");
        }
    }

    public ModuleLicenseModel(LicenseInfo license, String id, RepoPath repoPath) {
        this.repoPath = repoPath;
        this.license = license;
        this.id = id;
    }

    /**
     * create not found model
     * @param path - artifact path
     * @return - license module
     */
    public static ModuleLicenseModel createNotFoundModel(RepoPath path) {
        ModuleLicenseModel model = new ModuleLicenseModel(path);
        model.extractedLicense = LicenseInfo.NOT_FOUND_LICENSE;
        model.setModelProperties(false);
        return model;
    }

    public void populateFields(Dependency dependency) {
        this.md5 = dependency.getMd5();
        this.sha1 = dependency.getSha1();
        if (StringUtils.isNotBlank(dependency.getId())) {
            this.id = dependency.getId();
        }
        if ((dependency.getScopes() == null || dependency.getScopes().isEmpty()) || dependency.getScopes().contains(null)) {
            this.scopes.add(BuildService.UNSPECIFIED_SCOPE);
        } else {
            setScopes(dependency.getScopes());
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public void setScopes(Set<String> scopes) {
        this.scopes = scopes;
        ScopeForView(scopes);
    }

    /**
     * return scope in view table format
     * @param scopes
     */
    private void ScopeForView(Set<String> scopes) {
        if (scopes != null && !scopes.isEmpty()) {
            StringBuilder scopesBuilder = new StringBuilder();
            scopes.forEach(scope -> scopesBuilder.append(scope + " , "));
            String scopeString = scopesBuilder.toString();
            if (scopeString.length() > 0) {
                scopeNames = scopeString.substring(0, scopesBuilder.toString().length() - 3);
            }
        }
    }

    public String getScopeNames() {
        return scopeNames;
    }

    public LicenseInfo getLicense() {
        return license;
    }

    public void setLicense(LicenseInfo license) {
        this.license = license;
    }

    public LicenseInfo getExtractedLicense() {
        return extractedLicense;
    }

    public void setExtractedLicense(LicenseInfo extractedLicense) {
        this.extractedLicense = extractedLicense;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isOverridable() {
        return overridable;
    }

    public boolean hasConflicts() {
        return hasConflicts;
    }

    public boolean isNotFound() {
        return notFound;
    }

    public boolean isNeutral() {
        return neutral;
    }

    public void setNeutral(boolean isNeutral) {
        this.neutral = isNeutral;
    }

    /**
     * This method should be called after populating all licenses (property based and extracted), it will mark
     * conflict, not found, and overridable status properties
     */
    public void setModelProperties(boolean canAnnotate) {
        //Null proof just in case
        if (license == null) {
            license = LicenseInfo.NOT_FOUND_LICENSE;
        }
        if (extractedLicense == null) {
            extractedLicense = LicenseInfo.NOT_SEARCHED_LICENSE;
        }
        //Both licenses not found (or extracted was not searched) - mark model as not found
        if ((license.isNotFound() && extractedLicense.isNotFound())
                || (license.isNotFound() && extractedLicense.isNotSearched())) {
            hasConflicts = true;
            notFound = true;
            actions.remove("EditLicense");
        } else {
            overridable = hasMismatchingOverridableLicenses() && canAnnotate;
            //No property-based license info
            hasConflicts = license.isNotFound()
                    //Conflicting licenses - mismatch between extracted(if it was searched) and property-based
                    || hasMismatchingLicenses()
                    //property-based license found and unapproved
                    || (license.isFound() && !license.isApproved())
                    //Property-based license is unknown
                    || license.isUnknown();
        }
    }

    /**
     * true if property-based and extracted licenses are inconsistent, in case the extracted license was searched for
     */
    private boolean hasMismatchingLicenses() {
        return !extractedLicense.isNotSearched() && !license.equals(extractedLicense);
    }

    /**
     * true if model has an extracted license that can override(i.e. has 'meaningful' data) the property-based one
     * when running in auto mode (i.e property license if not found or empty unknown)
     */
    public boolean hasMismatchingAutoOverridableLicenses() {
        return extractedLicense.isFound() && (license.isNotFound()
                || (license.equals(EMPTY_UNKNOWN_LICENSE) && !extractedLicense.equals(EMPTY_UNKNOWN_LICENSE)));
    }

    /**
     * true if the model has mismatching licenses and the extracted license is valid - doesn't allow overriding any
     * property-based license with an unknown license.
     * Use only with UI, the auto run (CI) has another variant of overridable model decision.
     */
    private boolean hasMismatchingOverridableLicenses() {
        return hasMismatchingLicenses() && extractedLicense.isValidLicense();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModuleLicenseModel that = (ModuleLicenseModel) o;

        if (!getId().equals(that.getId())) {
            return false;
        } else if (!license.equals(that.license)) {
            return false;
        } else if (!extractedLicense.equals(that.extractedLicense)) {
            return false;
        } else if ((getRepoPath() != null) && !getRepoPath().equals(that.getRepoPath())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = 31 * getId().hashCode();
        result = 31 * result + license.hashCode();
        result = 31 * result + extractedLicense.hashCode();
        if (getRepoPath() != null) {
            result = 31 * result + getRepoPath().hashCode();
        }
        return result;
    }

    public String getDisplayName() {
        return getId();
    }

    public RepoPath getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(RepoPath repoPath) {
        this.repoPath = repoPath;
    }

    public String getRepoKey() {
        return repoKey;
    }

    public void setRepoKey(String repoKey) {
        this.repoKey = repoKey;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

}
