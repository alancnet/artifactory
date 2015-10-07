describe('unit test:artifactory dao', function () {
    var artifactoryDao;
    var RESOURCE;
    var $resource;
    var server;
    var artifactoryNotificationsInterceptor;
    var expectedActions;
    function initExpectedActions() {
        expectedActions = {
            'update': {
                method: 'PUT',
                interceptor: artifactoryNotificationsInterceptor
            },
            'delete': {
                method: 'DELETE',
                interceptor: artifactoryNotificationsInterceptor
            },
            'fetch': {
                method: 'POST',
            },
            'save': {
                method: 'POST',
                interceptor: artifactoryNotificationsInterceptor
            }
        };
    };
    // inject the DAO module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function (ArtifactoryDao, _$resource_, _RESOURCE_, _$httpBackend_, _artifactoryNotificationsInterceptor_) {
        artifactoryDao = ArtifactoryDao;
        RESOURCE = _RESOURCE_;
        server = _$httpBackend_;
        artifactoryNotificationsInterceptor = _artifactoryNotificationsInterceptor_;
        $resource = spyOn(ArtifactoryDao, '$resource').and.callThrough();
        initExpectedActions();
    }));

    it('should return a $resource', function() {
        expect(artifactoryDao.getInstance().name).toBe('Resource');
    });
    //
    it('should have default custom actions', function() {
        artifactoryDao.getInstance();
        expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
    });

    it('should allow to set URL', function() {
        artifactoryDao.setUrl('my url')
            .getInstance();
        expect($resource).toHaveBeenCalledWith('my url', null, expectedActions);
    });

    it('should allow to set relative path', function() {
        artifactoryDao.setPath('/mypath')
            .getInstance();
        expect($resource).toHaveBeenCalledWith(RESOURCE.API_URL + '/mypath', null, expectedActions);
    });
    describe('custom actions', function() {
        it('should allow to add custom actions', function () {
            artifactoryDao.setCustomActions({action: {}})
                .getInstance();
            expectedActions.action = {};
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
        it('should allow to override existing custom actions', function () {
            artifactoryDao.setCustomActions({update: {method: 'POST'}})
                .getInstance();
            expectedActions.update.method = 'POST';
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
        it('should allow to set relative path on actions', function() {
            artifactoryDao.setCustomActions({action: {path: '/actionpath'}})
                .getInstance();
            expectedActions.action = {url: RESOURCE.API_URL + '/actionpath'};
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
        it('should allow to add notifications interceptor on actions', function() {
            artifactoryDao.setCustomActions({action: {notifications: true}})
                .getInstance();
            expectedActions.action = {interceptor: artifactoryNotificationsInterceptor};
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
    });
    describe('setDefaults', function() {
        it('should allow to set default method for custom actions', function () {
            artifactoryDao.setDefaults({method: 'POST'})
                .setCustomActions({action: {}})
                .getInstance();
            expectedActions.action = {method: 'POST'};
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
        it('should allow to override default method in custom action', function () {
            artifactoryDao.setDefaults({method: 'POST'})
                .setCustomActions({action: {method: 'PUT'}})
                .getInstance();
            expectedActions.action = {method: 'PUT'};
            expect($resource).toHaveBeenCalledWith(null, null, expectedActions);
        });
    });
    describe('extendPrototype', function() {
        it('should allow to extend the prototype of the resource', function () {
            var method = function() {};
            var resource = artifactoryDao.extendPrototype({method: method})
                .getInstance();
            expect(resource.prototype.method).toBe(method);
        });
    });
});