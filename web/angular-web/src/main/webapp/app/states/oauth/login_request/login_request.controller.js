export class LoginRequestController {
    constructor(User) {
        User.getOAuthLoginData().then((response) => {
            this.links = response;
        });
    }
}
