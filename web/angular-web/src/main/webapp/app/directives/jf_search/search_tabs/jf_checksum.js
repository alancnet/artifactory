import EVENTS   from '../../../constants/artifacts_events.constants';
import TOOLTIP  from '../../../constants/artifact_tooltip.constant';

class jfChecksumController{
    constructor($state,ArtifactoryEventBus) {

        this.artifactoryEventBus=ArtifactoryEventBus;
        this.$state=$state;
        this.query.search= "checksum";
        this.TOOLTIP = TOOLTIP.artifacts.search.checksumSearch;
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

export function jfChecksum() {
    return {
        restrict: 'EA',
        scope:{
            query:'='
        },
        controller: jfChecksumController,
        controllerAs: 'jfChecksum',
        bindToController: true,
        templateUrl: 'directives/jf_search/search_tabs/jf_checksum.html'
    }
}