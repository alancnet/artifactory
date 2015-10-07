export class VirtualRepositoryFormController {
    constructor($scope, RepositoriesDao, parseUrl, ArtifactoryNotifications, ArtifactoryFeatures) {
        this.$scope = $scope;
        this.parseUrl = parseUrl;
        this.gridP2Option = {};
        this.repositoriesDao = RepositoriesDao;
        this.notifications = ArtifactoryNotifications;
        this.artifactoryGridFactory = $scope.RepositoryForm.artifactoryGridFactory;
        this.repositoryForm = $scope.RepositoryForm;
        this.virtualRepo = {};
        this.features = ArtifactoryFeatures;
        this._createGrid();
        this._initVirtual();
    }

    isSigningKeysDisable() {
        if (this.features.isDisabled("signingKeys")) {
            return true;
        }
        else {
            return false;
        }
    }

    _initVirtual() {
        if (!this.repositoryForm.newRepository) {
            if (this.repositoryForm.repoInfo.typeSpecific && this.repositoryForm.repoInfo.typeSpecific.p2Repos) {
                this.gridP2Option.setGridData(this.repositoryForm.repoInfo.typeSpecific.p2Repos);
            }
        }
        this.repositoriesDao.remoteUrlToRepoMap().$promise.then((result)=> {
            this.remoteUrlMap = result;
        });
    }

    _createGrid() {
        this.gridP2Option = this.artifactoryGridFactory.getGridInstance(this.$scope)
                .setColumns(this.getP2Columns())
                .setRowTemplate('default')
                .setButtons(this._getActions())
                .setGridData([]);
    }

    addP2Local() {
        let repoToPush = {};
        let baseUrl = 'local://';
        if (this.virtualRepo.pathSuffix) {
            repoToPush.pathSuffix = this.virtualRepo.pathSuffix.replace(/\/+/, '');
            repoToPush.repoUrl = baseUrl + this.virtualRepo.localRepoKey + "/" + this.virtualRepo.pathSuffix.replace(/\/+/, '');
        }
        else {
            repoToPush.repoUrl = baseUrl + this.virtualRepo.localRepoKey;
        }
        repoToPush.repoKey = this.virtualRepo.localRepoKey;
        if (this._repoKeyExists(repoToPush.repoKey)) {
            repoToPush.action = 'included';
        }
        else {
            repoToPush.action = 'include';
        }
        this._pushToGrid(repoToPush);
    }

    _repoKeyExists(repoKey) {
        let repos = this.repositoryForm.repoInfo.typeSpecific.p2Repos;
        if (repos) {
            return _.find(repos, {repoKey: repoKey});
        }
        return false;
    }
    _repoUrlExists(repoUrl) {
        let repos = this.repositoryForm.repoInfo.typeSpecific.p2Repos;
        if (repos) {
            return _.find(repos, {repoUrl: repoUrl});
        }
        return false;
    }

    addP2Remote() {
        let indexRepo = 1;
        let findMatch = false;
        this.currentRepo = '';
        _.forOwn(this.remoteUrlMap, (remoteUrl, key) => {
            if (this.virtualRepo.remoteUrl.startsWith(remoteUrl)) {
                let action = 'include';
                if (this._repoKeyExists(key)) {
                    action = 'included';
                }

                this._pushToGrid({repoKey: key, repoUrl: this.virtualRepo.remoteUrl, action: action});
                findMatch = true;
                return true;
            }
        });
        if (!findMatch) {
            let fields = this.repositoryForm.fields;
            let allRepos = fields.availableLocalRepos.concat(fields.availableRemoteRepos).concat(fields.availableVirtualRepos);
            let parser = this.parseUrl(this.virtualRepo.remoteUrl);

            this.currentRepo = parser.host.replace(':', "-");

            if (_.indexOf(allRepos, this.currentRepo) != -1) {
                let regexp = new RegExp(this.currentRepo + '-.+');
                let matchingRepos = _.select(allRepos, (repo) => {
                    return regexp.test(repo);
                });
                if (matchingRepos.length) {
                    let lastMatchingRepo = _.last(matchingRepos.sort());
                    indexRepo = lastMatchingRepo.substring(lastMatchingRepo.indexOf('-') + 1);
                    indexRepo = parseInt(indexRepo) + 1;
                }
                this.currentRepo = this.currentRepo + "-" + indexRepo;
            }

            this.remoteUrlMap[this.currentRepo] = this.virtualRepo.remoteUrl;
            this._pushToGrid({repoKey: this.currentRepo, repoUrl: this.virtualRepo.remoteUrl, action: 'create'});
        }
    }

    onchangeRepo() {
        this.repositoriesDao.getResolvedRepositories(
                this.repositoryForm.repoInfo).$promise.then((resolvedRepositories)=> {
                    this.repositoryForm.repoInfo.basic.resolvedRepositories = resolvedRepositories;
                });
    }

    _pushToGrid(repo) {
        if (this._repoUrlExists(repo.repoUrl)) {
            this.notifications.create({error: "Repo URL already exists in the list"});
            return;
        }
        this.repositoryForm.repoInfo.typeSpecific.p2Repos = this.repositoryForm.repoInfo.typeSpecific.p2Repos || [];
        this.repositoryForm.repoInfo.typeSpecific.p2Repos.push(repo);
        this.gridP2Option.setGridData(this.repositoryForm.repoInfo.typeSpecific.p2Repos);
    }

    _deleteRepo(repo) {
        _.remove(this.repositoryForm.repoInfo.typeSpecific.p2Repos, {repoUrl: repo.repoUrl});
        this.gridP2Option.setGridData(this.repositoryForm.repoInfo.typeSpecific.p2Repos);
    }

    getP2Columns() {
        return [
            {
                name: 'Action',
                displayName: 'Action',
                field: 'action',
                cellTemplate: '<div class="ui-grid-cell-contents">{{ row.entity.action || "included" }}</div>'
            },
            {
                name: 'Repository',
                displayName: 'Repository',
                field: 'repoKey',
                enableCellEdit: `{{row.entity.action === 'create'}}`

            }, {
                name: 'URL',
                displayName: 'URL',
                field: 'repoUrl',
                enableCellEdit: true
            }
        ]
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                callback: (repo) => {
                    this._deleteRepo(repo);
                }
            }
        ];
    }
}