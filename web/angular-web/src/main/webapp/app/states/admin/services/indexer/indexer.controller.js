import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminServicesIndexerController {
    constructor(IndexerDao, RepositoriesDao) {
        this.indexerDao = IndexerDao.getInstance();
        this.repositoriesDao = RepositoriesDao;
        this.indexer = {};
        this.TOOLTIP = TOOLTIP.admin.services.mavenIndexer;
        this.getIndexerObject();
    }

    getIndexerObject() {
        this.indexerDao.get().$promise.then((result) => {
            this.indexer = result;
            this.getRepoData();
        });
    }

    getRepoData() {
        if (!this.indexer.includedRepos) {
            this.repositoriesDao.indexerAvailableRepositories({type: 'Maven', layout: 'maven-2-default'}).$promise.then((repos) => {
                this.indexer.includedRepos = [];
                this.indexer.excludedRepos = [];
                this.indexer.includedRepos = repos.availableLocalRepos.concat(repos.availableRemoteRepos).concat(repos.availableVirtualRepos);
            });
        }
    }

    runIndexer() {
        this.indexerDao.run(this.indexer);
    }

    save(indexer) {
        this.indexerDao.save(indexer);
    }

    cancel() {
        this.getIndexerObject();
    }
}