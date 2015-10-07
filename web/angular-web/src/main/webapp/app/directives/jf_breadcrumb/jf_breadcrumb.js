import EVENTS from '../../constants/artifacts_events.constants';

class jfBreadcrumbController {

    constructor($scope, $state, $stateParams, ArtifactoryEventBus, $timeout) {

        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.$state = $state;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$timeout = $timeout;
        this._registerEvents();

        this.updateBreadcrumb();
    }

    updateBreadcrumb() {
        this.$timeout(()=>{
            switch(this.$state.current.name) {
                case 'builds.all':
                    this._initCrumbs();
                    break;
                case 'builds.history':
                    this._initCrumbs();
                    this.crumbs.push({
                        name:this.$stateParams.buildName,
                        state:this.$state.current.name + '(' + JSON.stringify(this.$stateParams) + ')'
                    });
                    break;
                case 'builds.info':
                    this._initCrumbs();
                    this.crumbs.push({
                        name: this.$stateParams.buildName,
                        state: 'builds.history' + '(' + JSON.stringify({buildName:this.$stateParams.buildName}) + ')'
                    });
                    this.crumbs.push({
                        name: this.$stateParams.buildNumber,
                        state: 'builds.info' + '(' + JSON.stringify({buildName: this.$stateParams.buildName ,buildNumber: this.$stateParams.buildNumber}) + ')'                    });
                    break;
            }
        });
    }

    _initCrumbs() {
        this.crumbs = [
            {name:'All Builds',state:'builds.all({})'}
        ];
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.BUILDS_BREADCRUMBS, () => {
            this.updateBreadcrumb();
        });
    }


}

export function jfBreadcrumb() {

    return {
        restrict: 'E',
        scope: {
        },
        controller: jfBreadcrumbController,
        controllerAs: 'jfBreadcrumb',
        templateUrl: 'directives/jf_breadcrumb/jf_breadcrumb.html',
        bindToController: true
    };
}
