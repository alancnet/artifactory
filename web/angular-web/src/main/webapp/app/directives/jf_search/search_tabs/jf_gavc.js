import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIP  from '../../../constants/artifact_tooltip.constant';

class jfGavcController {
    constructor($state, ArtifactoryEventBus) {

        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$state = $state;
        this.query.search = "gavc";
        this.TOOLTIP = TOOLTIP.artifacts.search.gavcSearch;
    }
    clearFields(){
        this.query={};
        this.query.search = "gavc";
    }

    search() {

        this.$state.go('.', {
            'searchType': this.query.search,
            'searchParams': {
                selectedRepos: this.query.selectedRepositories
            },
            'params': btoa(JSON.stringify(this.query))
        });
    }
}

export function jfGavc() {
    return {
        restrict: 'EA',
        scope: {
            query: '='
        },
        controller: jfGavcController,
        controllerAs: 'jfGavc',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_gavc.html'
    }
}
