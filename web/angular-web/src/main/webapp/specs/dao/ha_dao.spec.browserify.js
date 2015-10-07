import {regularResponse as haMock} from '../../mocks/ha_mock.browserify.js';

describe('unit test:ha dao', () => {
    let haDao;
    let RESOURCE;
    let server;

    // inject the main module
    beforeEach(m('artifactory.dao'));

    // run this code before each case
    beforeEach(inject( ($injector) => {
        haDao = $injector.get('HaDao');
        RESOURCE = $injector.get('RESOURCE');
        server = $injector.get('$httpBackend');
    }));

    it('haDao should return a resource object',  () => {
        expect(haDao.name).toBe('Resource');
    });

    it('send a get request with ha dao ',  () => {
        server.expectGET(RESOURCE.API_URL + RESOURCE.HIGH_AVAILABILITY).respond(haMock);
        haDao.query();
        server.flush();
    });
    it('send a delete request with ha dao ',  () => {
        server.expectDELETE(RESOURCE.API_URL + RESOURCE.HIGH_AVAILABILITY + '/art-8080').respond(200);
        haDao.delete({id: haMock[0].id});
        server.flush();
    });

});