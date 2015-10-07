import ART_EVENTS     from '../../../../constants/artifacts_events.constants';
import COMM_EVENTS from '../../../../constants/common_events.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

class jfStashInfoController {
    constructor($state, BrowseFilesDao, StashResultsDao, ArtifactoryEventBus) {
        this.$state = $state;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.stashResultsDao = StashResultsDao;
        this.artifactoryEventBus = ArtifactoryEventBus;

        this.TOOLTIP = TOOLTIP.admin.import_export.stash;

        this.exportOptions = {};

        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };


    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    clearValidations() {
        this.artifactoryEventBus.dispatch(COMM_EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    export() {
        let payload = {
            path: this.exportOptions.path,
            excludeMetadata: this.exportOptions.excludeMetadata || false,
            m2: this.exportOptions.createM2CompatibleExport || false,
            createArchive: this.exportOptions.createArchive || false,
            verbose: this.exportOptions.verbose || false
        };

        this.stashResultsDao.export({name: 'stash'},payload).$promise.then((response)=>{
//            console.log(response);
        });
    }

    gotoSearch() {
        this.artifactoryEventBus.dispatch(ART_EVENTS.SEARCH_URL_CHANGED,{searchType:'quick'});
    }

}

export function jfStashInfo() {
    return {
        restrict: 'EA',
        scope: {
            currentNode: '=',
            allowExport: '='
        },
        controller: jfStashInfoController,
        controllerAs: 'jfStashInfo',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_stash_info.html'
    }
}