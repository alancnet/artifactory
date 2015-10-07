describe('Unit: ArtifactoryStorage Service', function () {

    var artifactoryStorage;
    var TEST_KEY = 'TEST';
    var ITEM = {uid: 2582};

    // inject the main module
    beforeEach(m('artifactory.services'));

    // run this code before each case
    beforeEach(inject(function ($injector) {
        artifactoryStorage = $injector.get('ArtifactoryStorage');
    }));


    it('artifactoryStorage should be defined', function () {
        expect(artifactoryStorage).toBeDefined();
    });

    it('should save and retrieve an object from storage', function () {
        expect(artifactoryStorage.setItem(TEST_KEY,ITEM)).toEqual(  ITEM)
    });

    it('should be able to remove an item by key', function () {
        expect(artifactoryStorage.setItem(TEST_KEY,ITEM)).toEqual(ITEM);
        artifactoryStorage.removeItem(TEST_KEY);
        expect(artifactoryStorage.getItem(TEST_KEY)).toBeNull();
    });

    it('should return default value if not exist', function () {
        expect(artifactoryStorage.getItem(TEST_KEY, 10)).toBe(10);
    });

    it('should not return default value if exists', function () {
        artifactoryStorage.setItem(TEST_KEY,ITEM);
        expect(artifactoryStorage.getItem(TEST_KEY, 10)).toEqual(ITEM);
    });

});