import TOOLTIP from '../../../constants/artifact_tooltip.constant';

export class AdminRepositoryLayoutFormController {
    constructor($state,$stateParams, RepositoriesLayoutsDao) {
        this.$state = $state;
        this.$stateParams = $stateParams;
        this.layoutsDao = RepositoriesLayoutsDao;
        this.TOOLTIP = TOOLTIP.admin.repositories.layoutsForm;

        this.input = {};
        this.testReply = null;
        this.regexViewData = null;

        this.testReplyDictionary = {
            organization: 'Organization',
            module: 'Module',
            baseRevision: 'Base Revision',
            folderIntegrationRevision: 'Folder Integration Revision',
            fileIntegrationRevision: 'File Integration Revision',
            classifier: 'Classifier',
            ext: 'Extension',
            type: 'Type'
        };

        this.viewOnly = ($stateParams.viewOnly === true);

        if ($stateParams.layoutname) {
            this.mode = 'edit';
            this.layoutName = $stateParams.layoutname;
            this.title = 'Edit ' + this.layoutName + ' Repository Layout';
            this._getLayoutData(this.layoutName);
        }
        else if ($stateParams.copyFrom) {
            this.mode = 'create';
            this.title = 'New Repository Layout';
            this._getLayoutData($stateParams.copyFrom);
        }
        else {
            this.mode = 'create';
            this.title = 'New Repository Layout';
            this.layoutData = {};
        }

    }

    save() {
        if (this.mode == 'edit')
        {
            let payload = angular.copy(this.layoutData);
            delete (payload.repositoryAssociations);

            this.layoutsDao.update({},payload).$promise.then((data)=>{
                this.$state.go('^.repo_layouts');
            });
        }

        if (this.mode == 'create')
        {
            this.layoutsDao.save({},this.layoutData).$promise.then((data)=>{
                this.$state.go('^.repo_layouts');
            });
        }
    }

    hasAnyAssoc() {
        return this.layoutData.repositoryAssociations.localRepositories.length ||
               this.layoutData.repositoryAssociations.remoteRepositories.length ||
               this.layoutData.repositoryAssociations.virtualRepositories.length;
    }

    cancel() {
        this.$state.go('^.repo_layouts');
    }


    test() {
        let payload = angular.copy(this.layoutData);
        delete (payload.repositoryAssociations);
        _.extend(payload,{pathToTest: this.input.testPath});
        this.testReply = null;

        this.layoutsDao.testArtifactPath({},payload).$promise.then((data)=>{
            this.testReply = data;
        });
    }

    isSaveDisabled() {
        return !this.layoutForm || this.layoutForm.$invalid;
    }

    resolveRegex() {
        let payload = angular.copy(this.layoutData);
        delete (payload.repositoryAssociations);
        this.regexViewData = null;
        this.layoutsDao.resolveRegex({},payload).$promise.then((data)=>{
            this.regexViewData = data;
        });
    }

    gotoEditRepo(type,repo) {
        this.$state.go('admin.repositories.edit',{repoType: type, repoKey: repo});
    }
    _getLayoutData(layoutName) {
        this.layoutsDao.getLayoutData({},{layoutName:layoutName}).$promise.then((data)=>{
            this.layoutData = data;
            if (this.$stateParams.copyFrom) {
                this.layoutData.name = '';
            }
        });
    }

}
