export class AdminController {

    constructor($state, AdminMenuItems, ArtifactoryState, User, ArtifactoryFeatures) {
        this.items = AdminMenuItems;
        this.state = $state;
        this.user = User.getCurrent();
        this.artifactoryState = ArtifactoryState;
        this.features = ArtifactoryFeatures;
        this._goToSpecificAdminState();
    }

    _goToSpecificAdminState() {
        if (this.state.current.name !== 'admin') {
            this.artifactoryState.setState('lastAdminState', this.state.current);
            this.artifactoryState.setState('lastAdminStateParams', this.state.params);
            return;
        }
        
        let state = this.artifactoryState.getState('lastAdminState');
        let stateParams = this.artifactoryState.getState('lastAdminStateParams');
        let feature = state && state.params && state.params.feature;
        if (!state ||
            !this.user.canView(state.name) ||
            this.features.isDisabled(feature) ||
            this.features.isHidden(feature)) {
            state = this.user.isAdmin() ? 'admin.repositories.list' : 'admin.security.permissions';
            stateParams = this.user.isAdmin() ? {repoType: 'local'} : {};
        }
        this.state.go(state, stateParams);
    }

    isDashboard() {
        return this.state.$current.includes['admin.dashboard']
    }

}