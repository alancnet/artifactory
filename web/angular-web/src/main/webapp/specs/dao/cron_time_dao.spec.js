describe('unit test:crontime dao', function () {

    var cornTimeDao;
    var RESOURCE;
    var server;
    var cornMock = {
        "cron": "0 23 5 * * ?"
    };

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        cronTimeDao = $injector.get('CronTimeDao').getInstance();
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('cornTimeDao should return a resource object', function () {
      //  console.log(cornTimeDao);
       expect(cronTimeDao.name).toBe('Resource');
    });
    //
    it('should send a get request to return an cornTimeDao', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.CRON_TIME).respond(200);
        cronTimeDao.get();
        server.flush();
    })
});