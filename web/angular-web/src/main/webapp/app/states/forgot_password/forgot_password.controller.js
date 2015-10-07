import EVENTS     from '../../constants/common_events.constants';

const EMAIL_SENT_MESSAGE = "Reset password email was sent. \nDidn't received it? Contact your system administrator.";

export class ForgotPasswordController {

    constructor($state, User, ArtifactoryNotifications, ArtifactoryEventBus) {
        this.user = {};
        this.UserService = User;
        this.$state = $state;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.forgotPasswordForm = null;
        this.message = '';
    }

    forgot() {
        let self = this;

        this.artifactoryEventBus.dispatch(EVENTS.FORM_SUBMITTED);
        if (this.forgotPasswordForm.$valid) {
            this.UserService.forgotPassword(this.user).then(success, error)
        } else {
            form.user.$dirty = true;
        }

        function success(result) {
            self.$state.go('login');
            self.artifactoryNotifications.create({info: EMAIL_SENT_MESSAGE});
        }

        function error(errors) {
            self.$state.go('login');
        }
    }
}