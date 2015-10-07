angular.module('artifactory.mocks', [])
	.run(function($window) {
		var store = {};
		spyOn($window.localStorage, 'getItem').and.callFake((key) => {
			return store[key];
		});
		spyOn($window.localStorage, 'setItem').and.callFake((key, value) => {
			return store[key] = value;
		});
		spyOn($window.localStorage, 'removeItem').and.callFake((key) => {
			delete store[key];
		});
	});

// The mock function loads the above module before each
export default function mock() {
	beforeEach(m('artifactory.mocks'));
}