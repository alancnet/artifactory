import EVENTS from '../../constants/common_events.constants.js';

export class UserProfileController {

    constructor($state,UserProfileDao, BintrayDao, ArtifactoryNotifications, User, ArtifactoryEventBus) {
        this.userProfileDao = UserProfileDao;
        this.bintrayDao = BintrayDao.getInstance();
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.currentUser = User.getCurrent();
        this.artifactoryEventBus = ArtifactoryEventBus;

        this.userInfo = {};
        this.currentPassword = null;
        this.showUserApiKey = false;
        this.showBintrayApiKey = false;
        this.profileLocked = true;

        User.getLoginData().then((response) => {
            this.oauthProviderLink = response.oauthProviderLink;
        });


        if(this.currentUser.name=='anonymous'){
            $state.go('home');
        }
    }

    unlock() {
        this.userProfileDao.fetch({password: this.currentPassword}).$promise
            .then(response => {
                this.userInfo = response.data;
                //console.log(this.userInfo);
                this.profileLocked = false;
            });
    }

    save() {
        if (this.userInfo.user.newPassword && this.userInfo.user.newPassword !== this.userInfo.user.retypePassword) {
            this.artifactoryNotifications.create({error: 'Passwords do not match'});
            return;
        }

        let params = {
            user: {
                email: this.userInfo.user.email,
                password: this.userInfo.user.newPassword
            },
            bintray: this.userInfo.bintray
        };
        this.userProfileDao.update(params);
    }

    testBintray() {
        this.artifactoryEventBus.dispatch(EVENTS.FORM_SUBMITTED, this.bintrayForm.$name);
        this.bintrayDao.fetch(this.userInfo.bintray);
    }

}