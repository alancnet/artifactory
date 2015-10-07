import EVENTS from '../../../../constants/common_events.constants';
import API from '../../../../constants/api.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class ImportExportRepositoriesController {
    constructor(BrowseFilesDao, ExportDao, ImportDao, ArtifactoryNotifications, FileUploader, RepoDataDao, ArtifactoryEventBus) {
        this.repoDataDao = RepoDataDao;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.FileUploader = FileUploader;

        this.exportDao = ExportDao;
        this.importDao = ImportDao;
        this.TOOLTIP = TOOLTIP.admin.import_export.repositories;


        this.exportFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export',
            pathLabel: 'Path to export',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        };
        this.importFileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Import',
            pathLabel: 'Path to import',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: false
        };



        this.uploadZip = {};
        this.uploadSuccess = false;
        this.exportOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            excludeMetadata: false,
            m2: false,
            verbose: false
        };
        this.importOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            excludeMetadata: false,
            verbose: false
        };
        this.zipOptions = {
            action: 'repository',
            repository: 'All Repositories',
            path: '',
            verbose: false
        };
        this.artifactoryNotifications = ArtifactoryNotifications;
        this._initImportExportRepo();

    }

    _getRootPath() {
        this.browseFilesDao.query({path: '/'}).$promise.then((result)=> {
            if (result) {
                this.rootPath = result.roots[0] || '/';
                this.roots = result.roots;
            }
        });
    }

    _initImportExportRepo() {
        this.uploader = new this.FileUploader();
        this.uploader.url = `${API.API_URL}/artifactimport/upload`;
        this.uploader.onSuccessItem = this.onUploadSuccess.bind(this);
        this.uploader.onErrorItem = this.onUploadError.bind(this);
        this.uploader.onAfterAddingFile = this.onAddingfile.bind(this);

        this.uploader.removeAfterUpload = true;
        this.repoDataDao.get().$promise.then((result)=> {
            result.repoList.unshift('All Repositories');
            this.reposList = result.repoList;
        });

        this._getRootPath();
    }

    onUploadError(fileDetails, response) {
        this.artifactoryNotifications.create(response);
    }

    onUploadSuccess(fileDetails, response) {
        this.uploadSuccess = true;
        this.zipOptions.path = response.path;
    }

    onAddingfile(fileItem) {
        if (fileItem.file.size < 0) {
            fileItem.okToUploadFile = false;
            this.uploader.removeFromQueue(fileItem);
        }
        else {
            fileItem.okToUploadFile = true;
        }
    }

    updateExportFolderPath(directory) {
        this.exportOptions.path = directory;
    }

    updateImportFolderPath(directory) {
        this.importOptions.path = directory;
    }

    clearValidations() {
        this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

    export(form) {
        let self = this;
        if (form.$valid) {
            let ok = false;
            for (let i in this.roots) {
                let root = this.roots[i];
                if (_.startsWith(this.exportOptions.path.toUpperCase(), root)) {
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                this.exportOptions.path = this.defaultRoot + this.exportOptions.path;
            }
            this.exportDao.save(this.exportOptions);
        }
    }


    import(form) {
        if (form.$valid) {
            this.importDao.save(this.importOptions);
        }
    }

    importUploadZip() {
        let importDetails = {
            path: this.zipOptions.path,
            verbose: this.zipOptions.verbose,
            repository: this.zipOptions.repository,
            zip: true
        };
        this.importDao.save({action: 'repository'}, importDetails).$promise
            .finally(() => this.uploadSuccess = false);
    }

    upload() {
        this.uploader.queue[0].upload();
    }

}
