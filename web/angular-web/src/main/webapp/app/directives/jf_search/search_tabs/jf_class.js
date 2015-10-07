import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIP  from '../../../constants/artifact_tooltip.constant';

class jfClassController {
    constructor($state, ArtifactoryEventBus,$stateParams) {

        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$state = $state;
        this.query.search = 'class';
        this.query.searchClassOnly = ($stateParams.searchParams) ? $stateParams.searchParams.searchClassOnly : true;
        this.TOOLTIP = TOOLTIP.artifacts.search.classSearch;
    }

    search() {
        this.$state.go('.', {
            'searchType': "class",
            'searchParams': {
                searchClassOnly: this.query.searchClassOnly,
                selectedRepos: this.query.selectedRepositories
            },
            'params': btoa(JSON.stringify(this.query))
        });
    }
}

export function jfClass() {
    return {
        restrict: 'EA',
        scope: {
            query: '='
        },
        controller: jfClassController,
        controllerAs: 'jfClass',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_class.html'
    }
}
