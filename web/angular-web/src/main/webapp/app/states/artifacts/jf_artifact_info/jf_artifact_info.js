import EVENTS from '../../../constants/artifacts_events.constants';
import DICTIONARY from './../constants/artifact_general.constant';

class jfArtifactInfoController {
    constructor($element, $stateParams, $state, $scope, ArtifactoryEventBus, $timeout, User) {
        this.$element = $element;
        this.stateParams = $stateParams;
        this.state = $state;
        this.$timeout = $timeout;
        this.user = User;
        this.DICTIONARY = DICTIONARY.tabs;
        this.isDropdownOpen = false;
        this.artifactoryEventBus = ArtifactoryEventBus;
        ArtifactoryEventBus.registerOnScope($scope, EVENTS.TREE_NODE_SELECT, node => this.selectNode(node));
        $scope.$on('ui-layout.resize', () => this._refreshTabs());

    }

    selectNode(node) {
        if (node.data) {
            // wait for the element to render and calculate how many tabs should display
            if (!angular.equals(this.infoTabs, node.data.tabs)) {
                this._refreshTabs();
            }
            this.infoTabs = node.data.tabs;
            this._transformInfoTabs();
            this.currentNode = node;
            // if current tab exists in the new node - dispatch an event:
            if (_.findWhere(this.infoTabs, {name: this.stateParams.tab}) && this.stateParams.tab !== 'StashInfo') {
                this.artifactoryEventBus.dispatch(EVENTS.TAB_NODE_CHANGED, node);
            }
        }
        else {
            this.currentNode = null;
        }
    }

    _refreshTabs() {
        this.artifactoryEventBus.dispatch(EVENTS.TABS_REFRESH);
    }

    _transformInfoTabs() {
        let features = {
            'Watch': 'watches',
            'Properties': 'properties',
            'Builds': 'builds',
            'BlackDuck': 'blackDuck'
        };
        this.infoTabs.forEach((tab) => {
            tab.feature = features[tab.name];
        });
    }
}
export function jfArtifactInfo() {
    return {
        restrict: 'E',
        controller: jfArtifactInfoController,
        controllerAs: 'jfArtifactInfo',
        templateUrl: 'states/artifacts/jf_artifact_info/jf_artifact_info.html',
        bindToController: true
    }
}



