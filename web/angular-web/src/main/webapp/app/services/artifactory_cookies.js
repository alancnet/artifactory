export class ArtifactoryCookies {

    constructor($cookies) {
        this.cookies = $cookies;
    }

    getCookie(cookieName) {
        return this.cookies[cookieName]
    }

}