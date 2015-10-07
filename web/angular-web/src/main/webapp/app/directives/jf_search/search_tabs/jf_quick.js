import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIP  from '../../../constants/artifact_tooltip.constant';

class jfQuickController {
    constructor($scope, $state, ArtifactoryEventBus) {

        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$state = $state;
        this.TOOLTIP = TOOLTIP.artifacts.search.quickSearch;
    }

    search() {
        this.query.search = "quick";
        this.$state.go('.', {
            'searchType': this.query.search,
            'searchParams': {
                selectedRepos: this.query.selectedRepositories
            },
            'params': btoa(JSON.stringify(this.query))
        });
    }

}

export function jfQuick() {
    return {
        scope: {
            query: '='
        },
        restrict: 'EA',
        controller: jfQuickController,
        controllerAs: 'jfQuick',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_quick.html'
    }
}
