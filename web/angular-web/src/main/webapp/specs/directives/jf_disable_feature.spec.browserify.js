'use strict';
import FooterMock from '../../mocks/footer_mock.browserify';

let FooterDao, $httpBackend, $scope, divJqueryElement, anchorJqueryElement, $timeout;

describe('unit test:jf_disable_feature directive', () => {
  function compileDirective(feature) {
    $scope = compileHtml('<div jf-disable-feature="' + feature + '"><a href="#">first link</a><a href="#">second link</a></div>');
    $timeout.flush();
    divJqueryElement = $('[jf-disable-feature]');
    anchorJqueryElement = $('[jf-disable-feature] a');
  }
  
  function setup(_FooterDao_, _$httpBackend_, _$timeout_) {
  	FooterDao = _FooterDao_;
  	$httpBackend = _$httpBackend_;
  	$timeout = _$timeout_;
	}

  function getFooterData() {
  	FooterDao.get(true);
  	$httpBackend.flush();
  }

  function expectToBeEnabled() {
		expect(anchorJqueryElement).not.toHaveAttr('disabled');
		expect(divJqueryElement).not.toHaveClass('license-required');
  }

  function expectToBeDisabled() {
		expect(anchorJqueryElement).toHaveAttr('disabled');
  }

  function expectToHaveClass(license) {
		expect(divJqueryElement).toHaveClass('license-required');
		expect(divJqueryElement).toHaveClass('license-required-' + license);
  }

  function expectToBeHidden() {
		expect(anchorJqueryElement).toBeHidden();
  }
  function expectToBeVisible() {
		expect(anchorJqueryElement).toBeVisible();
  }

  beforeEach(m('artifactory.templates', 'artifactory.directives'));
  beforeEach(inject(setup));

  describe('OSS license', () => {
  	new FooterMock().mockOss();
  	beforeEach(getFooterData);
	  it('should disable a PRO feature', () => {
			compileDirective('stash');
			expectToBeDisabled();
			expectToHaveClass('PRO');
	  });
	  it('should disable an ENT feature', () => {
			compileDirective('highavailability');
			expectToBeDisabled();
			expectToHaveClass('ENT');
	  });
	  it('should hide a hidden OSS feature', () => {
			compileDirective('register_pro');
			expectToBeHidden();
	  });
  });

  describe('PRO license', () => {
  	new FooterMock().mockPro();
  	beforeEach(getFooterData);
	  it('should not disable a PRO feature', () => {
			compileDirective('stash');
			expectToBeEnabled();
	  });
	  it('should hide an ENT feature', () => {
			compileDirective('highavailability');
			expectToBeHidden();
	  });
	  it('should not hide a hidden OSS feature', () => {
			compileDirective('register_pro');
			expectToBeVisible();
	  });
  });

  describe('ENT license', () => {
  	new FooterMock().mockEnt();
  	beforeEach(getFooterData);
	  it('should not disable a PRO feature', () => {
			compileDirective('stash');
			expectToBeEnabled();
	  });
	  it('should not disable an ENT feature', () => {
			compileDirective('highavailability');
			expectToBeEnabled();
	  });
	  it('should not hide a hidden OSS feature', () => {
			compileDirective('register_pro');
			expectToBeVisible();
	  });
  });

  describe('AOL', () => {
  	new FooterMock().mockAol();
  	beforeEach(getFooterData);
	  it('should hide for AOL', () => {
			compileDirective('backups');
			expectToBeHidden();
	  });
  });
});
