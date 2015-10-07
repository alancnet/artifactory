package org.artifactory.ui.rest.service.artifacts.browse;

import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.properties.DeletePropertyModel;
import org.artifactory.ui.rest.model.artifacts.browse.treebrowser.tabs.watchers.DeleteWatchersModel;
import org.artifactory.ui.rest.service.artifacts.browse.generic.BrowseNativeService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.*;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.deleteversions.DeleteVersionService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.actions.deleteversions.GetVersionUnitsService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.blackduck.GetBlackDuckArtifactService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.blackduck.UpdateBlackDuckComponentIdService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.bower.BowerViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.builds.GetArtifactBuildJsonService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.builds.GetArtifactBuildsService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.checksums.FixChecksumsService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker.DockerAncestryViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker.DockerV2ViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.docker.DockerViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.gemsview.GemsViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.GetArtifactsCount;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.GetDependencyDeclarationService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.GetGeneralArtifactsService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.SetFilteredResourceService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.bintray.GetGeneralBintrayService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses.GetAllAvailableLicensesService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses.GetArchiveLicenseFileService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses.QueryCodeCenterService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses.ScanArtifactForLicensesService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.general.licenses.SetLicensesOnPathService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.npmview.NpmViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.nugetview.NugetViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.permission.GetEffectivePermissionService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties.CreatePropertyService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties.DeletePropertyService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties.GetPropertyService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.properties.UpdatePropertyService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.pypi.PypiViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.rpm.RpmViewService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.viewsource.ArchiveViewSourceService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers.GetWatchersService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers.RemoveWatchersService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tabs.watchers.WatchStatusService;
import org.artifactory.ui.rest.service.artifacts.browse.treebrowser.tree.BrowseTreeNodesService;
import org.springframework.beans.factory.annotation.Lookup;

/**
 * @author Chen Keinan
 */
public abstract class BrowseServiceFactory {

    // fetch tree service
    @Lookup
    public abstract BrowseTreeNodesService getBrowseTreeNodesService();
    // fetch general artifacts info
    @Lookup
    public abstract GetGeneralArtifactsService getGetGeneralArtifactsService();

    @Lookup
    public abstract GetGeneralBintrayService getGetGeneralBintrayService();

    @Lookup
    public abstract GetArtifactsCount getArtifactsCount();

    @Lookup
    public abstract GetDependencyDeclarationService getGetDependencyDeclarationService();
    // property service
    @Lookup
    public abstract CreatePropertyService getCreatePropertyService();

    @Lookup
    public abstract GetPropertyService getGetPropertyService();

    @Lookup
    public abstract UpdatePropertyService getUpdatePropertyService();

    @Lookup
    public abstract DeletePropertyService<DeletePropertyModel> getDeletePropertyService();

    @Lookup
    public abstract GetEffectivePermissionService getGetEffectivePermissionService();

    @Lookup
    public abstract GemsViewService gemsViewService();

    @Lookup
    public abstract NpmViewService npmViewService();

    // watchers service
    @Lookup
    public abstract GetWatchersService getWatchersService();

    @Lookup
    public abstract RemoveWatchersService<DeleteWatchersModel> getRemoveWatchersService();

    // builds service
    @Lookup
    public abstract GetArtifactBuildsService getArtifactBuildsService();

    @Lookup
    public abstract GetArtifactBuildJsonService getArtifactBuildJsonService();

    // action services
    @Lookup
    public abstract DownloadArtifactService downloadArtifactService();

    @Lookup
    public abstract GetDownloadFolderInfoService getDownloadFolderInfo();

    @Lookup
    public abstract DownloadFolderArchiveService downloadFolder();

    @Lookup
    public abstract WatchArtifactService watchArtifactService();

    @Lookup
    public abstract CopyArtifactService copyArtifactService();

    @Lookup
    public abstract MoveArtifactService moveArtifactService();

    @Lookup
    public abstract ZapArtifactService zapArtifactService();

    @Lookup
    public abstract ZapCachesVirtualService zapCachesVirtual();

    @Lookup
    public abstract DeleteArtifactService deleteArtifactService();

    @Lookup
    public abstract ViewArtifactService viewArtifactService();
    // watch status
    @Lookup
    public abstract WatchStatusService watchStatusService();
    // delete versions
    @Lookup
    public abstract GetVersionUnitsService getDeleteVersionsService();

    @Lookup
    public abstract DeleteVersionService deleteVersionService();
    // view source
    @Lookup
    public abstract ArchiveViewSourceService archiveViewSourceService();

    @Lookup
    public abstract NugetViewService nugetViewService();

    @Lookup
    public abstract RpmViewService rpmViewService();

    @Lookup
    public abstract PypiViewService pypiViewService();

    @Lookup
    public abstract BowerViewService bowerViewService();

    @Lookup
    public abstract DockerViewService dockerViewService();

    @Lookup
    public abstract DockerV2ViewService dockerV2ViewService();

    @Lookup
    public abstract DockerAncestryViewService dockerAncestryViewService();

    @Lookup
    public abstract GetBlackDuckArtifactService getBlackDuckArtifact();

    @Lookup
    public abstract UpdateBlackDuckComponentIdService updateBlackDuckComponentId();

    @Lookup
    public abstract FixChecksumsService fixChecksums();

    @Lookup
    public abstract SetFilteredResourceService setFilteredResource();

    @Lookup
    public abstract GetAllAvailableLicensesService getAllAvailableLicenses();

    @Lookup
    public abstract SetLicensesOnPathService setLicensesOnPath();

    @Lookup
    public abstract ScanArtifactForLicensesService scanArtifactForLicenses();

    @Lookup
    public abstract QueryCodeCenterService queryCodeCenter();

    @Lookup
    public abstract GetArchiveLicenseFileService getArchiveLicenseFile();

    @Lookup
    public abstract BrowseNativeService browseNative();

    @Lookup
    public abstract RecalculateIndexService recalculateIndex();

}
