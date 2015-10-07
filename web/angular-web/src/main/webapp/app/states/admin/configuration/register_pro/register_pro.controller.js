import EVENTS from '../../../../constants/artifacts_events.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminConfigurationRegisterController{

    constructor(RegisterProDao, ArtifactoryEventBus,User, $state) {
        this.registerProDao = RegisterProDao;
        this.$state = $state;
        this.ArtifactoryEventBus = ArtifactoryEventBus;
        this.User=User;
        this.TOOLTIP = TOOLTIP.admin.configuration.registerPro;
        this.getData();
    }

    save(registerDetails) {
        this.registerProDao.update(registerDetails).$promise.then( (data)=> {
            // Refresh the home page footer with the new license details
            this.ArtifactoryEventBus.dispatch(EVENTS.FOOTER_REFRESH);
            this.User.loadUser(true);
            if (data.status === 200) this.$state.go('home');
        });
    }

    getData() {
        if(!this.User.currentUser.isProWithoutLicense()) this.registerDetails = this.registerProDao.get();
    }
}