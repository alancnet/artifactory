describe('parseUrl', () => {
	let parseUrl, parser, url;
	function setup(_parseUrl_) {
		parseUrl = _parseUrl_;
	}
	beforeEach(m('artifactory.services'));
	beforeEach(inject(setup));
	beforeEach(() => {
		url = 'http://www.google.com:8080/test?a=b';
    parser = parseUrl(url);
	});
	it('should return the host', () => {
    expect(parser.host).toEqual('www.google.com:8080');
	});
	it('should return the href', () => {
    expect(parser.href).toEqual(url);
	});
});