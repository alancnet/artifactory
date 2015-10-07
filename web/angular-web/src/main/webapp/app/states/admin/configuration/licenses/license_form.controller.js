import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminConfigurationLicenseFormController {
    constructor($stateParams, LicensesDao, $state, ArtifactoryState) {
    	this.state = $state;
    	this.isNew = !$stateParams.licenseName;
    	this.licensesDao = LicensesDao;
		this.artifactoryState = ArtifactoryState;
        this.TOOLTIP = TOOLTIP.admin.configuration.licenseForm;

    	if (this.isNew) {
    		this.license = new LicensesDao();
    	}
    	else {
    		this.license = LicensesDao.getSingleLicense({name: $stateParams.licenseName});
    	}
    }

    save() {
		let whenSaved = this.isNew ? this.license.$create() : this.license.$update();
        whenSaved.then(() => this._end());

    }
	cancel() {
        this._end();
    }
    _end() {
        let prevState = this.artifactoryState.getState('prevState');
        if (prevState) {
            this.state.go(prevState.state,prevState.params);
        }
        else {
            this.state.go('^.licenses');
        }
    }

    testRegex(value) {

        let regex = new RegExp('^[A-Za-z0-9\._-]*$');
        return regex.test(value);
    }

}