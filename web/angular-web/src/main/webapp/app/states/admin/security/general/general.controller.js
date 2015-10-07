import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecurityGeneralController {

    constructor(AdminSecurityGeneralDao, PasswordsEncryptionDao) {
        this.adminSecurityGeneralDao = AdminSecurityGeneralDao;
        this.passwordsEncryptionDao = PasswordsEncryptionDao.getInstance();
        this.options = ['SUPPORTED', 'UNSUPPORTED', 'REQUIRED'];
        this.TOOLTIP = TOOLTIP.admin.security.general;

        this.getGeneralConfigObject();
        this.getMasterKeyStatus();
    }

    getEncryptionButtonText() {
        return this.materKeyState.hasMasterKey ? "Decrypt" : "Encrypt";
    }

    getEncryptionStatusText() {
        return this.materKeyState.hasMasterKey ? "All passwords in your configuration are currently encrypted." :
                "All passwords in your configuration are currently visible in plain text.";
    }

    getGeneralConfigObject() {
        this.adminSecurityGeneralDao.get().$promise.then((data) => {
            this.generalConfig = data;
        });
    }

    getMasterKeyStatus() {
        this.materKeyState = this.passwordsEncryptionDao.get();
    }

    toggleEncryption() {
        let self = this;

        if (this.materKeyState.hasMasterKey) {
            this.materKeyState.$decrypt().then(function () {
                self.getMasterKeyStatus();
            })
        } else {
            this.materKeyState.$encrypt().then(function () {
                self.getMasterKeyStatus();
            });
        }
    }

    save() {
        this.adminSecurityGeneralDao.update(this.generalConfig);
    }

    cancel() {
        this.getGeneralConfigObject();
    }

    onClickAllowAnonymousAccess() {
        if (!this.generalConfig.anonAccessEnabled) {
            this.generalConfig.anonAccessToBuildInfosDisabled = false;
        }
    }
}