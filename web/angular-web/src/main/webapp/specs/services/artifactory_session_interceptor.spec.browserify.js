import UserMock from '../../mocks/user_mock.browserify.js';
describe('Unit: User Service', function () {
    var User;
    var ArtifactoryEventBus;
    var ArtifactoryState;
    var $state;
    var RESOURCE;
    var sessionInterceptor;
    var path = 'path';
    var res = {
        headers:null,
        config: {
            url: null
        }
    };

    var USER_KEY = 'USER';

    function mockSessionValid(value) {
        res.headers = function() {
            return {sessionvalid: value && 'true' || 'false'};
        };
    }

    function mockApiRequest(value) {
        res.config.url = value && RESOURCE.API_URL || '';
    }

    function setup(_User_, _ArtifactoryEventBus_, _$state_, _RESOURCE_, artifactorySessionInterceptor, $location, _ArtifactoryState_) {
        RESOURCE = _RESOURCE_;
        ArtifactoryState = _ArtifactoryState_;
    	User = _User_;
    	ArtifactoryEventBus = _ArtifactoryEventBus_;
    	$state = _$state_;
        sessionInterceptor = artifactorySessionInterceptor;
    	spyOn(ArtifactoryEventBus, 'dispatch');
        spyOn(ArtifactoryState, 'setState');
    	spyOn($state, 'go');
        spyOn($location, 'path').and.returnValue(path);
    }

    function mockGuest(isGuest) {
        var user = isGuest ? UserMock.guest() : new UserMock();
        user.mockUserMethods();
    }

    function expectExpireSession() {
        it('should reload the user from localstorage', function() {
            expect(User.loadUser).toHaveBeenCalled();
        });
        it('should reload user from server', function() {
            expect(User.loadUser).toHaveBeenCalledWith(true);
        });
        it('should send the user to login page', function() {
            expect($state.go).toHaveBeenCalledWith('login');
        });
        it('should save path for after login', function() {
            expect(ArtifactoryState.setState).toHaveBeenCalledWith('urlAfterLogin', path);
        });
    }

    function expectNotExpireSession() {
        it('should not reload user from server', function() {
            expect(User.loadUser).not.toHaveBeenCalledWith(true);
        });
        it('should save path for after login', function() {
            expect(ArtifactoryState.setState).not.toHaveBeenCalled();
        });
        it('should not send the user to login page', function() {
            expect($state.go).not.toHaveBeenCalled();
        });
    }

    // inject the services module
    beforeEach(m('artifactory.services'));
    beforeEach(inject(setup));

    describe('session expired', function() {
        beforeEach(function() {
            mockGuest(false);
            mockApiRequest(true);
            mockSessionValid(false);
            sessionInterceptor.response(res);
        });
        expectExpireSession();
    });
    describe('session invalid but was guest', function() {
        beforeEach(function() {
            mockGuest(true);
            mockApiRequest(true);
            mockSessionValid(false);
            sessionInterceptor.response(res);
        });
        it('should reload the user from localstorage', function() {
            expect(User.loadUser).toHaveBeenCalled();
        });
        expectNotExpireSession();
    });
    describe('session invalid but not api request', function() {
        beforeEach(function() {
            mockGuest(false);
            mockApiRequest(false);
            mockSessionValid(false);
            sessionInterceptor.response(res);
        });
        it('should reload the user from localstorage', function() {
            expect(User.loadUser).toHaveBeenCalled();
        });
        expectNotExpireSession();
    });
    describe('session valid', function() {
        beforeEach(function() {
            mockGuest(false);
            mockApiRequest(true);
            mockSessionValid(true);
            sessionInterceptor.response(res);
        });
        it('should reload the user from localstorage', function() {
            expect(User.loadUser).toHaveBeenCalled();
        });
        expectNotExpireSession();
    });
    describe('currently in login page', function() {
        beforeEach(function() {
            $state.current = 'login';
            mockGuest(false);
            mockApiRequest(true);
            mockSessionValid(false);
            sessionInterceptor.response(res);
        });
        it('should reload the user from localstorage', function() {
            expect(User.loadUser).toHaveBeenCalled();
        });
        it('should reload user from server', function() {
            expect(User.loadUser).toHaveBeenCalledWith(true);
        });
        it('should not send the user to login page', function() {
            expect($state.go).not.toHaveBeenCalledWith('login');
        });
    });
    describe('session expired but bypass interceptor', function() {
        beforeEach(function() {
            mockGuest(false);
            mockApiRequest(true);
            mockSessionValid(true);
            res.config.bypassSessionInterceptor = true;
            sessionInterceptor.response(res);
        });
        it('should not reload the user from localstorage', function() {
            expect(User.loadUser).not.toHaveBeenCalled();
        });
        expectNotExpireSession();
    });
});