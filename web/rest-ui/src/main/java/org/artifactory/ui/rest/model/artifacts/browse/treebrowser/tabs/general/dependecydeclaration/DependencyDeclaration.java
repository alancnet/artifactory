package org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.general.dependecydeclaration;

import org.apache.commons.lang.StringUtils;
import org.artifactory.api.maven.MavenArtifactInfo;
import org.artifactory.api.module.ModuleInfo;
import org.artifactory.api.module.ModuleInfoBuilder;
import org.artifactory.api.repo.RepositoryService;
import org.artifactory.descriptor.repo.LocalRepoDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.fs.ItemInfo;
import org.artifactory.rest.common.model.BaseModel;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.util.RepoLayoutUtils;

/**
 * @author Chen Keinan
 */
public class DependencyDeclaration extends BaseModel {

    private String[] types;
    private String dependencyData;

    public String[] getTypes() {
        return types;
    }

    public void setTypes(String[] types) {
        this.types = types;
    }

    public String getDependencyData() {
        return dependencyData;
    }

    public void setDependencyData(String dependencyData) {
        this.dependencyData = dependencyData;
    }

    /**
     * update dependency declaration
     *
     * @param artifactoryRestRequest - encapsulate data related to request
     * @param repoService            - repository service
     * @param itemInfo               - repository item info
     * @param localRepoDescriptor    - repository descriptor
     */
    public void updateDependencyDeclaration(ArtifactoryRestRequest artifactoryRestRequest,
            RepositoryService repoService, ItemInfo itemInfo, LocalRepoDescriptor localRepoDescriptor) {
        RepoLayout repoLayout = localRepoDescriptor.getRepoLayout();
        ModuleInfo moduleInfo = getModuleInfo(repoService, itemInfo, repoLayout);
        if (moduleInfo != null && moduleInfo.isValid()) {
            if (artifactoryRestRequest.getQueryParamByKey("buildtool").length() > 0) {
                updateDependencyDeclarationData(artifactoryRestRequest, moduleInfo);
            }
            String[] buildTypes = {"Maven", "Ivy", "Gradle", "Sbt"};
            types = buildTypes;
        }
    }

    /**
     * get declaration data by type and update declaration dependency model
     *
     * @param artifactoryRestRequest - encapsulate data related to request
     * @param moduleInfo             - artifact module data
     * @return dependency declaration instance
     */
    private void updateDependencyDeclarationData(ArtifactoryRestRequest artifactoryRestRequest,
            ModuleInfo moduleInfo) {
        String declaration;
        String buildTool = artifactoryRestRequest.getQueryParamByKey("buildtool");
        DependencyDeclaration dependencyDeclaration = this;
        switch (buildTool) {
            case "maven":
                declaration = this.getMavenDependencyDeclaration(moduleInfo);
                break;
            case "gradle":
                declaration = this.getGradleDependencyDeclaration(moduleInfo);
                break;
            case "ivy":
                declaration = this.getIvyDependencyDeclaration(moduleInfo);
                break;
            case "sbt":
                declaration = this.getSbtDependency(moduleInfo);
                break;
            default:
                declaration = this.getMavenDependencyDeclaration(moduleInfo);
                break;
        }
        dependencyDeclaration.setDependencyData(declaration);
    }

    /**
     * get module info data
     *
     * @param repositoryService - repository service
     * @param itemInfo          - item info
     * @param repoLayout        repository layout
     * @return - item module info
     */
    private ModuleInfo getModuleInfo(RepositoryService repositoryService, ItemInfo itemInfo, RepoLayout repoLayout) {
        ModuleInfo moduleInfo = null;
        if (!itemInfo.isFolder()) {
            boolean defaultM2 = RepoLayoutUtils.isDefaultM2(repoLayout);
            if (defaultM2) {
                MavenArtifactInfo mavenArtifactInfo = MavenArtifactInfo.fromRepoPath(itemInfo.getRepoPath());
                if (mavenArtifactInfo.isValid()) {
                    moduleInfo = new ModuleInfoBuilder()
                            .organization(mavenArtifactInfo.getGroupId())
                            .module(mavenArtifactInfo.getArtifactId())
                            .baseRevision(mavenArtifactInfo.getVersion())
                            .classifier(mavenArtifactInfo.getClassifier())
                            .ext(mavenArtifactInfo.getType())
                            .build();
                }
            }
            if (moduleInfo == null) {
                moduleInfo = repositoryService.getItemModuleInfo(itemInfo.getRepoPath());
            }
        }
        return moduleInfo;
    }

    /**
     * get Gradle Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getGradleDependencyDeclaration(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder("compile(group: '").append(moduleInfo.getOrganization()).
                append("', name: '").append(moduleInfo.getModule()).append("', version: '").
                append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("'");

        String classifier = moduleInfo.getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            sb.append(", classifier: '").append(classifier).append("'");
        }

        String ext = moduleInfo.getExt();
        if (StringUtils.isNotBlank(ext) && !"jar".equalsIgnoreCase(ext)) {
            sb.append(", ext: '").append(ext).append("'");
        }
        return sb.append(")").toString();
    }

    /**
     * get sbt Dependency Declaration
     *
     * @param moduleInfo - item module info
     * @return Dependency Declaration as String
     */
    public String getSbtDependency(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder("libraryDependencies += ").
                append("\"").append(moduleInfo.getOrganization()).append("\"").
                append(" % ").
                append("\"").append(moduleInfo.getModule()).append("\"").
                append(" % ").
                append("\"").append(moduleInfo.getBaseRevision()).append("\"");
        return sb.toString();
    }

    /**
     * get Ivy Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getIvyDependencyDeclaration(ModuleInfo moduleInfo) {
        String module = moduleInfo.getModule();

        StringBuilder sb = new StringBuilder("<dependency org=\"").append(moduleInfo.getOrganization()).append("\" ")
                .append("name=\"").append(module).append("\" ").append("rev=\"").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("\"");

        String classifier = moduleInfo.getClassifier();
        String type = moduleInfo.getType();

        boolean validClassifier = StringUtils.isNotBlank(classifier);
        boolean validType = StringUtils.isNotBlank(type);

        if (validClassifier || !"jar".equals(type)) {
            sb.append(">\n")
                    .append("    <artifact name=\"").append(module).append("\"");

            if (validType && (validClassifier || !"jar".equals(type))) {
                sb.append(" type=\"").append(type).append("\"");
            }

            if (validClassifier) {
                sb.append(" m:classifier=\"").append(classifier).append("\"");
            }

            sb.append(" ext=\"").append(moduleInfo.getExt()).append("\"/>\n")
                    .append("</dependency>");
        } else {
            sb.append("/>");
        }
        return sb.toString();
    }


    /**
     * get Maven Dependency Declaration
     * @param moduleInfo  - item module info
     * @return Dependency Declaration as String
     */
    public String getMavenDependencyDeclaration(ModuleInfo moduleInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<dependency>\n");
        sb.append("    <groupId>").append(moduleInfo.getOrganization()).append("</groupId>\n");
        sb.append("    <artifactId>").append(moduleInfo.getModule()).append("</artifactId>\n");
        sb.append("    <version>").append(moduleInfo.getBaseRevision());

        String artifactRevisionIntegration = moduleInfo.getFileIntegrationRevision();
        if (StringUtils.isNotBlank(artifactRevisionIntegration)) {
            sb.append("-").append(artifactRevisionIntegration);
        }
        sb.append("</version>\n");

        String classifier = moduleInfo.getClassifier();
        if (StringUtils.isNotBlank(classifier)) {
            sb.append("    <classifier>").append(classifier).append("</classifier>\n");
        }

        String ext = moduleInfo.getExt();
        if (StringUtils.isNotBlank(ext) && !"jar".equalsIgnoreCase(ext)) {
            sb.append("    <type>").append(moduleInfo.getExt()).append("</type>\n");
        }

        return sb.append("</dependency>").toString();
    }
}
