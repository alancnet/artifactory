package org.artifactory.ui.rest.model.setmeup;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author chen keinan
 */
public class IvySettingModel extends BaseModel {

    private Set<String> libsRepository = Sets.newTreeSet((o1, o2) ->
            StringUtils.containsIgnoreCase(o1, "release") && !StringUtils.containsIgnoreCase(o1, "plugin") ? -1 : 1);
    private List<String> libsRepositoryLayout = new ArrayList<>();
    private String libsRepo;
    private String libsRepoLayout;
    private String libsResolverName;
    private Boolean useIbiblioResolver;
    private Boolean m2Compatible;
    private String ivySnippet;

    public IvySettingModel() {
    }

    public IvySettingModel(String ivySnippet) {
        this.ivySnippet = ivySnippet;
    }

    public Set<String> getLibsRepository() {
        return libsRepository;
    }

    public void setLibsRepository(Set<String> libsRepository) {
        this.libsRepository = libsRepository;
    }

    public List<String> getLibsRepositoryLayout() {
        return libsRepositoryLayout;
    }

    public void setLibsRepositoryLayout(List<String> libsRepositoryLayout) {
        this.libsRepositoryLayout = libsRepositoryLayout;
    }

    public String getLibsRepo() {
        return libsRepo;
    }

    public void setLibsRepo(String libsRepo) {
        this.libsRepo = libsRepo;
    }

    public String getLibsRepoLayout() {
        return libsRepoLayout;
    }

    public void setLibsRepoLayout(String libsRepoLayout) {
        this.libsRepoLayout = libsRepoLayout;
    }

    public String getLibsResolverName() {
        return libsResolverName;
    }

    public void setLibsResolverName(String libsResolverName) {
        this.libsResolverName = libsResolverName;
    }

    public Boolean getUseIbiblioResolver() {
        return useIbiblioResolver;
    }

    public void setUseIbiblioResolver(Boolean useIbiblioResolver) {
        this.useIbiblioResolver = useIbiblioResolver;
    }

    public Boolean getM2Compatible() {
        return m2Compatible;
    }

    public void setM2Compatible(Boolean m2Compatible) {
        this.m2Compatible = m2Compatible;
    }

    public String getIvySnippet() {
        return ivySnippet;
    }

    public void setIvySnippet(String ivySnippet) {
        this.ivySnippet = ivySnippet;
    }

    public void clearProps() {
        libsRepository = null;
        libsRepositoryLayout = null;
    }
}
