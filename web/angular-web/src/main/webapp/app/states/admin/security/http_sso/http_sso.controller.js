import EVENTS     from '../../../../constants/common_events.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminSecurityHttpSSoController {

    constructor(HttpSsoDao,ArtifactoryEventBus) {
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.httpSsoDao = HttpSsoDao.getInstance();
        this.sso = this.getSsoData();
        this.TOOLTIP = TOOLTIP.admin.security.HTTPSSO;
    }

    getSsoData() {
        this.httpSsoDao.get().$promise.then((sso)=> {
            this.sso = sso;
            this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
        });
    }

    save(sso) {
        this.httpSsoDao.update(sso);
    }
}