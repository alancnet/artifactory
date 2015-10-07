export function parseUrl() {
	return function(url) {
	    let parser = document.createElement('a');
	    parser.href = url;
	    return parser;
	}
}
