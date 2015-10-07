'use strict';
import FooterMock from '../../mocks/footer_mock.browserify';

let FooterDao, $httpBackend, $scope, jqueryElement;

describe('unit test:jf_disable_feature directive', () => {
  function compileDirective() {
    $scope = compileHtml('<div jf-hide-for-aol></div>');
    jqueryElement = $('[jf-hide-for-aol]');
  }
  
  function setup(_FooterDao_, _$httpBackend_) {
  	FooterDao = _FooterDao_;
  	$httpBackend = _$httpBackend_;
	}

  function getFooterData() {
  	FooterDao.get(true);
  	$httpBackend.flush();
  }

  function expectToBeHidden() {
		expect(jqueryElement).toBeHidden();
  }
  function expectToBeVisible() {
		expect(jqueryElement).toBeVisible();
  }

  beforeEach(m('artifactory.templates', 'artifactory.directives'));
  beforeEach(inject(setup));

  describe('AOL', () => {
  	new FooterMock().mockAol();
  	beforeEach(getFooterData);
	  it('should hide the element', () => {
			compileDirective();
			expectToBeHidden();
	  });
  });

  describe('not AOL', () => {
  	new FooterMock();
  	beforeEach(getFooterData);
	  it('should not hide the element', () => {
			compileDirective();
			expectToBeVisible();
	  });
  });
});
