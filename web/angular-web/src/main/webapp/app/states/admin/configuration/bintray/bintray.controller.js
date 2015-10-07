import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminConfigurationBintrayController {

    constructor(BintrayDao) {
        this.bintrayDao = BintrayDao.getInstance();
        this.TOOLTIP = TOOLTIP.admin.configuration.bintray;
        this._init();
    }

    _init() {

        this.bintrayDao.get().$promise.then((data)=>{
            this.bintray = data;
            this.bintray.fileUploadLimit = data.fileUploadLimit || 0;
        });
    }

    save(bintray) {
        this.bintrayDao.update(bintray);
    }

    cancel() {
        this._init();
    }

    fullCredentials() {
        return this.bintray && this.bintray.userName && this.bintray.apiKey;
    }

    testBintray() {
        this.bintrayDao.save(this.bintray).$promise
            .then(data => {});
    }
}