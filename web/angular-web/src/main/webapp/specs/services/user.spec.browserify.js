import UserMock from '../../mocks/user_mock.browserify.js';
var GUEST = 1;
var USER = 2;
var ADMIN = 3;
describe('Unit: User Service', function () {
    var User;
    var RESOURCE;
    var artifactoryStorage;
    var server;
    var $rootScope;
    var $window;
    var USER_KEY = 'USER';
    
    var user = new UserMock({name: 'admin'});
    var regularUser = UserMock.regularUser();
    var guestUser = UserMock.guest();
    var guestUserObj;
    var regularUserObj;
    var adminUserObj;


    useAngularEquality();

    // inject the main module
    beforeEach(m('artifactory.services'));

    // must run this before injecting User Service
    beforeEach(inject(function (ArtifactoryStorage) {
        artifactoryStorage = ArtifactoryStorage;
        artifactoryStorage.removeItem(USER_KEY);
    }));

    // run this code before each case
    function setup(initialUser) {
        if (initialUser) {
            artifactoryStorage.setItem(USER_KEY, initialUser);
        }
        inject(function ($injector, _RESOURCE_, _User_, _$rootScope_, _$window_) {
            RESOURCE = _RESOURCE_;
            User = _User_;
            server = $injector.get('$httpBackend');
            $rootScope = _$rootScope_;
            $window = _$window_;
            guestUserObj = guestUser.getUserObj();
            regularUserObj = regularUser.getUserObj();
            adminUserObj = user.getUserObj();
            server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(user);
            spyOn($window.location, 'replace');
        });
    }

    describe('static methods', () => {
        describe('start as guest', () => {
            beforeEach(() => {
                setup();
            });

            it('User should be defined', function () {
                expect(User).toBeDefined();
            });

            it('should save user data after logging in', function(done) {
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_LOGIN + 'false').respond(user);
                User.login({username: 'admin', password: '123123'}, false).then(() => {
                    var userResponse = artifactoryStorage.getItem(USER_KEY);
                    expect(userResponse).not.toBeNull();
                    expect(userResponse.name).toBe('admin');
                    done();
                });
                server.flush();
            });

            it('should reload user from server after logging out', function(done) {
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_IS_SAML).respond(false);
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_LOGOUT).respond(200);
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(guestUser);
                User.logout().then(() => {
                    expect(artifactoryStorage.getItem(USER_KEY)).toEqual(guestUser);
                    done();
                });
                server.flush();
            });

            //it('should redirect user to SAML logout', function(done) {
            //    server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_IS_SAML).respond(true);
            //    server.expectGET(RESOURCE.API_URL + RESOURCE.SAML_LOGOUT).respond('url');
            //    User.logout().then(() => {
            //        expect($window.location.replace).toHaveBeenCalledWith('url');
            //        done();
            //    });
            //    server.flush();
            //});

            it('should send password reset email', function(done) {
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_FORGOT_PASSWORD).respond(200);
                User.forgotPassword({username: 'admin'}).then(function(response) {
                    expect(response.status).toBe(200);
                    done();
                });
                server.flush();
            });

            it('should validate reset password key', function(done) {
                var key = 'YHSH8@(@&!773';
                var serverResponse = {"user": "admin"};
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_VALIDATE_KEY + key).respond(serverResponse);
                User.validateKey(key).then(function(response) {
                    expect(response.status).toBe(200);
                    expect(response.data.user).toBe('admin');
                    done();
                });
                server.flush();
            });

            it('should reset password', function(done) {
                var key = 'YHSH8@(@&!773';
                server.expectPOST(RESOURCE.API_URL + RESOURCE.AUTH_RESET_PASSWORD + key).respond(200);
                User.resetPassword(key, user).then(function(response) {
                    expect(response.status).toBe(200);
                    done();
                });
                server.flush();
            });

            it('should allow to setUser', () => {
                User.setUser(user);
                expect(artifactoryStorage.getItem(USER_KEY)).toEqual(user);
            });    

            it('should allow to getCurrent when there is no user', () => {
                expect(User.getCurrent()).toEqual({});
            });

            it('should allow to reload the user from localstorage', function(finito) {
                artifactoryStorage.setItem(USER_KEY, user);
                User.loadUser().then((user) => {
                    expect(user).toEqual(adminUserObj);
                    finito();
                });
                $rootScope.$digest();
            });

            it('should allow to reload the user from the server', function(voila) {
                server.expectGET(RESOURCE.API_URL + RESOURCE.AUTH_CURRENT).respond(regularUser);
                User.loadUser(true).then((response) => {
                    expect(response).toEqual(regularUserObj);
                    voila();
                });
                server.flush();
                $rootScope.$digest();
            });

            it('should have a whenLoadedFromServer variable', function(voila) {
                User.whenLoadedFromServer.then((response) => {
                    expect(response).toEqual(adminUserObj);
                    voila();
                });
                server.flush();
                $rootScope.$digest();
            });

            it('should not change reference after reloading from localstorage', () => {
                var getCurrent = User.getCurrent();
                artifactoryStorage.setItem(USER_KEY, user);
                User.loadUser();
                expect(User.getCurrent()).toBe(getCurrent);
            });
        });
        describe('start as logged in user', () => {
            beforeEach(() => {
                setup(user);
            });
            it ('should allow to getCurrent when there is a user', () => {
                expect(User.getCurrent()).toEqual(adminUserObj);
            });
        });
    });
    describe('instance methods', () => {
        it('should have an isGuest method', () => {
            expect(guestUserObj.isGuest()).toBeTruthy();
            expect(regularUserObj.isGuest()).toBeFalsy();
            expect(adminUserObj.isGuest()).toBeFalsy();
        });
        it('should have an isAdmin method', () => {
            expect(guestUserObj.isAdmin()).toBeFalsy();
            expect(regularUserObj.isAdmin()).toBeFalsy();
            expect(adminUserObj.isAdmin()).toBeTruthy();
        });
        it('should have an isRegularUser method', () => {
            expect(guestUserObj.isRegularUser()).toBeFalsy();
            expect(regularUserObj.isRegularUser()).toBeTruthy();
            expect(adminUserObj.isRegularUser()).toBeFalsy();
        });
        it('should have an isLoggedIn method', () => {
            expect(guestUserObj.isLoggedIn()).toBeFalsy();
            expect(regularUserObj.isLoggedIn()).toBeTruthy();
            expect(adminUserObj.isLoggedIn()).toBeTruthy();
        });
        it('should have a getCanManage method', () => {
            expect(guestUserObj.getCanManage()).toBeFalsy();
            expect(adminUserObj.getCanManage()).toBeTruthy();
        });
        it('should have a getCanDeploy method', () => {
            expect(guestUserObj.getCanDeploy()).toBeFalsy();
            expect(adminUserObj.getCanDeploy()).toBeTruthy();
        });
        it('should have a canView method', () => {
            expect(guestUserObj.canView('builds')).toBeFalsy();
            expect(adminUserObj.canView('builds')).toBeTruthy();
            expect(guestUserObj.canView('admin.security.permission')).toBeFalsy();
            expect(adminUserObj.canView('admin.security.permission')).toBeTruthy();
            expect(regularUserObj.canView('admin.security.general')).toBeFalsy();
            expect(adminUserObj.canView('admin.security.general')).toBeTruthy();
            expect(guestUserObj.canView('home')).toBeTruthy();
            expect(regularUserObj.canView('home')).toBeTruthy();
            expect(adminUserObj.canView('home')).toBeTruthy();
        });
    });
});