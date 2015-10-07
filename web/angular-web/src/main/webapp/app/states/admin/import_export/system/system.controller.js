import EVENTS from '../../../../constants/common_events.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminImportExportSystemController {
    constructor(BrowseFilesDao, ExportDao, ImportDao, ArtifactoryNotifications, ArtifactoryModal, ArtifactoryEventBus) {
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.systemExportDao = ExportDao;
        this.systemImportDao = ImportDao;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.modal = ArtifactoryModal;
        this.TOOLTIP = TOOLTIP.admin.import_export.system;

        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };
        this.importFileBrowserOptions = {
            canSelectFiles: true,
            selectionLabel: 'Directory Or Zip File To Import',
            pathLabel: 'Path to import',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: false
        };


        this.exportOptions = {
            path: '',
            excludeContent: false,
            excludeMetadata: false,
            excludeBuilds: false,
            m2: false,
            createArchive: false,
            verbose: false
        };

        this.importOptions = {
            path: '',
            excludeContent: false,
            excludeMetadata: false,
            verbose: false
        };

        this._getRootPath();
    }

    _getRootPath() {
        this.browseFilesDao.query({path: '/'}).$promise.then((result) => {
            if (result) {
                this.defaultRootPath = result.roots[0] || '/';
                this.roots = result.roots;
            }
        });
    }

    clearValidations() {
        this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    updateImportFolderPath(directory) {
        this.importOptions.path = directory;
    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    import() {
        if (this.importForm.$valid) {
            this.confirmImport();
        }
    }

    doImport() {
        this.importOptions.zip = _.endsWith(this.importOptions.path, '.zip');
        this.importOptions.action = "system";
        this.systemImportDao.save(this.importOptions);
    }

    export() {
        if (this.exportForm.$valid) {
            let ok = false;
            for (let i in this.roots) {
                let root = this.roots[i];
                if (_.startsWith(this.exportOptions.path.toUpperCase(), root)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                this.exportOptions.path = this.defaultRootPath + this.exportOptions.path;
            }
            this.exportOptions.action = "system";
            this.systemExportDao.save(this.exportOptions);
        }
    }

    confirmImport() {
        this.modal.confirm('Full system import deletes all existing Artifactory content. <br /> Are you sure you want to continue?')
                .then(() => this.doImport());
    }

}
