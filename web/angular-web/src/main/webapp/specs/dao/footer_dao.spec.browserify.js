var mockStorage = require('../../mocks/artifactory_storage_mock.browserify.js');

describe('unit test:footer dao', function () {

    var footerDao;
    var RESOURCE;
    var server;
    var footerResponse = {"isAol": false, "versionID": "PRO", "versionInfo":"Artifactory Professional","buildNumber":"4.x-SNAPSHOT rev ${buildNumber.prop}","licenseInfo":"Licensed to JFrog","copyRights":"© Copyright 2015 JFrog Ltd","copyRightsUrl":"http://www.jfrog.org"};
    mockStorage();

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        footerDao = $injector.get('FooterDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    //
    it('should send a get request to return an footerDao', function () {
        server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
        footerDao.get();
        server.flush();
    });
    it('should cache the result', function (finito) {
        server.expectGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(footerResponse);
        footerDao.get();
        footerDao.get()
            .then(function(footerInfo) {
                expect(footerDao.getInfo()).toEqual(footerInfo);
                finito();
            });
        server.flush();
    });

});