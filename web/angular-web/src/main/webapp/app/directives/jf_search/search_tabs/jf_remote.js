import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIP  from '../../../constants/artifact_tooltip.constant';

class jfRemoteController {
    constructor($state, ArtifactoryEventBus, RepositoriesDao, ArtifactoryNotifications, User, ArtifactoryModal) {

        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$state = $state;
        this.TOOLTIP = TOOLTIP.artifacts.search.remoteSearch;
        this.repositoriesDao = RepositoriesDao;
        this.notifications = ArtifactoryNotifications;
        this.modal = ArtifactoryModal;
        this.user = User.getCurrent();
        this.isJcenterExists = true;
        this.jcenterExists();
    }

    search() {
        this.query.search='remote';

        this.$state.go('.', {
            'searchType': this.query.search,
            'searchParams': {
                selectedRepos: this.query.selectedRepositories
            },
            'params': btoa(JSON.stringify(this.query))
        });
    }

    jcenterExists() {
        this.repositoriesDao.isJcenterRepoConfigured().$promise
        .then(() => this.isJcenterExists = true, () => this.isJcenterExists = false);
    }

    createJcenter() {
        if(!this.user.isAdmin()) {
            this.notifications.create({warn: 'Only an admin user can create repositories.'});
            return false;
        }
        this.modal.confirm('A remote repository pointing to JCenter with default configuration values is about to be created.<br/>' +
                'If you wish to change it\'s configuration you can do so from the Remote Repositories menu in the Admin section',
                'Creating JCenter remote repository')
                .then(()=> {
                    this.repositoriesDao.createDefaultJcenterRepo().$promise
                            .then(() => this.isJcenterExists = true , () => '');
                }
        );
    }
}

export function jfRemote() {
    return {
        restrict: 'EA',
        scope: {
            query: '='
        },
        controller: jfRemoteController,
        controllerAs: 'jfRemote',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_remote.html'
    }
}