import EVENTS from '../../../constants/artifacts_events.constants';
import DICTIONARY from '../constants/builds.constants';
const tabWidth = 165;
export class BuildsInfoController {
    constructor($stateParams, $timeout, User, ArtifactoryEventBus, ArtifactoryFeatures) {
        this.$stateParams = $stateParams;
        this.$timeout = $timeout;
        this.user = User.getCurrent();
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.features = ArtifactoryFeatures;
        this.DICTIONARY = DICTIONARY.tabs;
        this.buildTitle='Build #'+this.$stateParams.buildNumber;
        this.tabs = [
            {name: 'general'},
            {name: 'published'},
            {name: 'environment'},
            {name: 'issues'},
            {name: 'licenses', feature: 'licenses'},
            {name: 'governance', feature: 'blackduck'},
            {name: 'diff', feature: 'diff'},
            {name: 'history'},
            {name: 'json'}
        ];
        this.tabs.forEach((tab) => {
            tab.isDisabled = this._isTabDisabled(tab);
        });

        this.artifactoryEventBus.dispatch(EVENTS.BUILDS_BREADCRUMBS);
    }

    _isTabDisabled(tab) {
        return !this.user.canViewBuildState('builds.info', {tab: tab.name})
            || this.features.isDisabled(tab.feature);
    }
}