describe('Unit: Artifactory EventBus Service', function () {

    var scope;
    var eventBus;

    var EVENT_MOCKS = {
        EVENT_NAME: 'action:watch',
        EVENT_NAME2: 'action:copy',
        CALLBACK_FN: function () {}
    };

    // inject the main module
    beforeEach(m('artifactory.services'));

    // run this code before each case
    beforeEach(inject(function ($injector, $rootScope) {
        scope = $rootScope.$new();
        eventBus = $injector.get('ArtifactoryEventBus');
        spyOn(EVENT_MOCKS, 'CALLBACK_FN');

    }));

    it('should be initialized', function () {
        expect(eventBus).toBeDefined();
    });

    it('should allow to register and dispatch event', function () {
        eventBus.register(EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.CALLBACK_FN);
        eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        expect(EVENT_MOCKS.CALLBACK_FN).toHaveBeenCalled();
    });

    it('should allow to register events on scope and dispatch event', function () {
        eventBus.registerOnScope(scope, EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.CALLBACK_FN);
        eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        expect(EVENT_MOCKS.CALLBACK_FN).toHaveBeenCalled();
    });

    it('should allow to register multiple events on scope and dispatch event', function () {
        eventBus.registerOnScope(scope, [EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.EVENT_NAME2], EVENT_MOCKS.CALLBACK_FN);
        eventBus.dispatch(EVENT_MOCKS.EVENT_NAME2);
        expect(EVENT_MOCKS.CALLBACK_FN).toHaveBeenCalled();
        eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        expect(EVENT_MOCKS.CALLBACK_FN).toHaveBeenCalled();
    });

    it('should be able to deregister a callback', function () {
        var deregisterFunction = eventBus.register(EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.CALLBACK_FN);
        deregisterFunction();
        try {
            eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        } catch(e) {}
        expect(EVENT_MOCKS.CALLBACK_FN).not.toHaveBeenCalled();
    });

    it('should be able to deregister a callback on multiple events', function () {
        var deregisterFunctions = eventBus.register([EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.EVENT_NAME2], EVENT_MOCKS.CALLBACK_FN);
        deregisterFunctions[0]();
        try {
            eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        } catch(e) {}
        expect(EVENT_MOCKS.CALLBACK_FN).not.toHaveBeenCalled();
        eventBus.dispatch(EVENT_MOCKS.EVENT_NAME2);
        expect(EVENT_MOCKS.CALLBACK_FN).toHaveBeenCalled();
    });

    it('should deregister automatically on scope destroy', function () {
        eventBus.registerOnScope(scope, EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.CALLBACK_FN);
        scope.$destroy();
        try {
            eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
        } catch(e) {}
        expect(EVENT_MOCKS.CALLBACK_FN).not.toHaveBeenCalled();
    });
    it('should deregister automatically on scope destroy with multiple events', function () {
        eventBus.registerOnScope(scope, [EVENT_MOCKS.EVENT_NAME, EVENT_MOCKS.EVENT_NAME2], EVENT_MOCKS.CALLBACK_FN);
        scope.$destroy();
        try {
            eventBus.dispatch(EVENT_MOCKS.EVENT_NAME);
            eventBus.dispatch(EVENT_MOCKS.EVENT_NAME2);
        } catch(e) {}
        expect(EVENT_MOCKS.CALLBACK_FN).not.toHaveBeenCalled();
    });

});