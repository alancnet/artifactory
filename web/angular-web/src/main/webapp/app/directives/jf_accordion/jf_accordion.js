class jfAccordionController {

    constructor($state, $rootScope,ArtifactoryState,$timeout) {
        this.state = $state;
        this.currentAccordion = '';
        this.artifactoryState = ArtifactoryState;
        this.$timeout = $timeout;
        this.openItemByCurrentState();
        $rootScope.$on('$stateChangeSuccess', () => this.openItemByCurrentState());
    }

    openItemByCurrentState() {
        let item = _.find(this.items, (item) => {
            return this.isItemActive(item);
        });
        if (item) item.isOpen = true;
    }

    saveState() {
        this.$timeout(()=>{
            this.artifactoryState.setState('lastAdminState',this.state.current);
            this.artifactoryState.setState('lastAdminStateParams', this.state.params);
        });
    }

    isItemActive(item) {
        // (Adam) - don't use $state.includes, because it goes by the route hierarchy 
        let result = _.contains(this.state.current.name, item.state);
        // (Adam) - if item includes paramsParams, match this params with the current state
        if (result && item.stateParams) {
            let relevantParams = _.pick(this.state.params, Object.keys(item.stateParams));
            result = angular.equals(relevantParams, item.stateParams);
        }
        return result;
    }

}

export function jfAccordion() {

    return {
        restrict: 'EA',
        scope: {items: '='},
        controller: jfAccordionController,
        controllerAs: 'jfAccordion',
        templateUrl: 'directives/jf_accordion/jf_accordion.html',
        bindToController: true
    };
}

