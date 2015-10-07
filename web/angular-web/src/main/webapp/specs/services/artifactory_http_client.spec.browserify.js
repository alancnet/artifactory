describe('Unit test: ArtifactoryHttpClient', () => {
	let httpBackend;
	let ArtifactoryHttpClient;
	let data = {};
	beforeEach(m('artifactory.services'));
	function setup($httpBackend, RESOURCE, _ArtifactoryHttpClient_) {
		httpBackend = $httpBackend;
		RESOURCE.API_URL = '/base';
		ArtifactoryHttpClient = _ArtifactoryHttpClient_;
	}
	beforeEach(inject(setup));
	describe('post', () => {
		it('should make a post request', () => {
			httpBackend.expectPOST('/base/path', data, {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.post('/path', data);
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectPOST('/base/path', data, {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.post('/path', data, {headers: {'X-Custom1': 'value'}});
		});
	});

	describe('put', () => {
		it('should make a put request', () => {
			httpBackend.expectPUT('/base/path', data, {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.put('/path', data);
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectPUT('/base/path', data, {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.put('/path', data, {headers: {'X-Custom1': 'value'}});
		});
	});

	describe('get', () => {
		it('should make a get request', () => {
			httpBackend.expectGET('/base/path', {'Content-Type': 'application/json'});
			ArtifactoryHttpClient.get('/path');
		});

		it('should allow to extend the headers', () => {
			httpBackend.expectGET('/base/path', {'Content-Type': 'application/json', 'X-Custom1': 'value'});
			ArtifactoryHttpClient.get('/path', {headers: {'X-Custom1': 'value'}});
		});
	});
});