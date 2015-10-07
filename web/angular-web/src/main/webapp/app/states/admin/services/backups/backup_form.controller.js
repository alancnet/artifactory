import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

let $state, $stateParams, RepoDataDao, BackupDao;

export class AdminServicesBackupFormController {
    constructor(_$state_, _$stateParams_, _RepoDataDao_, _BackupDao_, BrowseFilesDao) {
        $state = _$state_;
        $stateParams = _$stateParams_;
        RepoDataDao = _RepoDataDao_;
        BackupDao = _BackupDao_;
        this.browseFilesDao = BrowseFilesDao.getInstance();
        
        this.isNew = !$stateParams.backupKey;
        this.TOOLTIP = TOOLTIP.admin.services.backupsForm;
        this.formTitle = `${this.isNew ? 'New' : 'Edit ' + $stateParams.backupKey} Backup`;
        this._initBackup();

        this.fileBrowserOptions = {
            canSelectFiles: false,
            selectionLabel: 'Directory To Export Backup',
            pathLabel: 'Path to Export Backup',
            confirmButtonLabel: 'Select',
            showSelectedItem: true,
            enableSelectedItem: true
        }
    }

    _initBackup() {
        if (this.isNew) {
            RepoDataDao.get().$promise.then((repoData) => {
                this.backup = {
                    enabled: true,
                    sendMailOnError: true,
                    retentionPeriodHours: 168,
                    includeRepos: repoData.repoList,
                    excludeRepos: []
                };
            });
        }
        else {
            BackupDao.get({key: $stateParams.backupKey}).$promise
                .then((backup) => this.backup = backup);
        }
    }

    updateFolderPath(directory) {
        this.backup.dir = directory;
    }

    save() {
        let whenSaved = this.isNew ? BackupDao.save(this.backup) : BackupDao.update(this.backup);
        whenSaved.$promise.then(() => this._end());
    }

    cancel() {
        this._end();
    }

    _end() {
        $state.go('^.backups');
    }

    onClickIncremental() {
        if (this.backup.incremental) {
            this.backup.retentionPeriodHours = 0;
            this.backup.createArchive=false;
        }
    }
    onClickZip() {
        if (this.backup.createArchive) {
            this.backup.incremental = false;
        }
    }

    runNow() {
        BackupDao.runNow({},this.backup).$promise.then((res)=>{
           //console.log(res);
        });
    }
}
