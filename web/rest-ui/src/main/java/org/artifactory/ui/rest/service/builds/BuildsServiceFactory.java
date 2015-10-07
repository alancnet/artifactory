package org.artifactory.ui.rest.service.builds;

import org.artifactory.ui.rest.model.builds.DeleteBuildsModel;
import org.artifactory.ui.rest.service.builds.bintray.GetBintrayArtifactService;
import org.artifactory.ui.rest.service.builds.bintray.GetBintrayPackagesService;
import org.artifactory.ui.rest.service.builds.bintray.GetBintrayRepositoriesService;
import org.artifactory.ui.rest.service.builds.bintray.GetBintrayVersionsService;
import org.artifactory.ui.rest.service.builds.bintray.PushArtifactToBintrayService;
import org.artifactory.ui.rest.service.builds.bintray.PushBuildToBintrayService;
import org.artifactory.ui.rest.service.builds.buildsinfo.DeleteAllBuildsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.DeleteBuildService;
import org.artifactory.ui.rest.service.builds.buildsinfo.GetAllBuildsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.GetBuildHistoryService;
import org.artifactory.ui.rest.service.builds.buildsinfo.GetPrevBuildListService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.BuildIssuesService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.BuildReleaseHistoryService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.GetBuildGeneralInfoService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.GetBuildJsonService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.BuildDiffService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.DiffBuildArtifactService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.DiffBuildDependenciesService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.DiffBuildModuleArtifactService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.DiffBuildModuleDependencyService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.builddiff.DiffBuildPropsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.env.GetEnvBuildPropsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.env.GetSystemBuildPropsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance.GetBuildGovernanceService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.governance.UpdateGovernanceRequestService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses.BuildLicensesService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses.ChangeBuildLicenseService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses.ExportLicenseToCsvService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.licenses.OverrideSelectedLicensesService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.publishedmodules.GetModuleArtifactsService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.publishedmodules.GetModuleDependencyService;
import org.artifactory.ui.rest.service.builds.buildsinfo.tabs.publishedmodules.GetPublishedModulesService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class BuildsServiceFactory {

    @Lookup
    public abstract GetAllBuildsService getAllBuilds();

    @Lookup
    public abstract GetBuildHistoryService getBuildHistory();

    @Lookup
    public abstract GetBuildGeneralInfoService getBuildGeneralInfo();

    @Lookup
    public abstract GetPublishedModulesService getPublishedModules();

    @Lookup
    public abstract GetModuleArtifactsService getModuleArtifacts();

    @Lookup
    public abstract GetModuleDependencyService getModuleDependency();

    @Lookup
    public abstract DeleteAllBuildsService deleteAllBuilds();

    @Lookup
    public abstract DeleteBuildService<DeleteBuildsModel> deleteBuild();

    @Lookup
    public abstract GetBuildJsonService getBuildJson();

    @Lookup
    public abstract DiffBuildModuleArtifactService diffBuildModuleArtifact();

    @Lookup
    public abstract DiffBuildArtifactService diffBuildArtifact();

    @Lookup
    public abstract DiffBuildDependenciesService diffBuildDependencies();

    @Lookup
    public abstract DiffBuildModuleDependencyService diffBuildModuleDependency();

    @Lookup
    public abstract GetPrevBuildListService getPrevBuildList();

    @Lookup
    public abstract DiffBuildPropsService diffBuildProps();

    @Lookup
    public abstract GetEnvBuildPropsService getEnvBuildProps();

    @Lookup
    public abstract GetSystemBuildPropsService getSystemBuildProps();

    @Lookup
    public abstract BuildIssuesService getBuildIssues();

    @Lookup
    public abstract BuildLicensesService buildLicenses();

    @Lookup
    public abstract BuildReleaseHistoryService buildReleaseHistory();

    @Lookup
    public abstract BuildDiffService buildDiff();

    @Lookup
    public abstract ExportLicenseToCsvService exportLicenseToCsv();

    @Lookup
    public abstract OverrideSelectedLicensesService overrideSelectedLicenses();

    @Lookup
    public abstract ChangeBuildLicenseService changeBuildLicense();

    @Lookup
    public abstract GetBuildGovernanceService getBuildGovernance();

    @Lookup
    public abstract UpdateGovernanceRequestService updateGovernanceRequest();

    @Lookup
    public abstract GetBintrayVersionsService getBintrayVersions();

    @Lookup
    public abstract GetBintrayPackagesService getBintrayPackages();

    @Lookup
    public abstract GetBintrayRepositoriesService getBintrayRepositories();

    @Lookup
    public abstract PushBuildToBintrayService pushToBintray();

    @Lookup
    public abstract PushArtifactToBintrayService pushArtifactToBintray();

    @Lookup
    public abstract GetBintrayArtifactService getBintrayArtifact();



}
