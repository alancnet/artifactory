import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecuritySamlIntegrationController {

    constructor(SamlDao) {
        this.samlDao = SamlDao.getInstance();
        this.TOOLTIP = TOOLTIP.admin.security.SAMLSSOSettings;

        this._init();
    }

    _init() {
        this.samlDao.get().$promise.then((data) => {
            this.saml = data;
            if (!angular.isDefined(this.saml.noAutoUserCreation)) {
                this.saml.noAutoUserCreation = true;
            }
        });
    }

    save() {
        this.samlDao.update(this.saml);
    }

    cancel() {
        this._init();
    }
    canSave() {
        return this.samlForm.$valid;
    }
}