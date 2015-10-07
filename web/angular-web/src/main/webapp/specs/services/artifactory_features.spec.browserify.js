'use strict';
import FooterMock from '../../mocks/footer_mock.browserify';
describe('ArtifactoryFeatures', () => {
	let ArtifactoryFeatures;

	beforeEach(m('artifactory.services', 'artifactory.dao'));
	function setup(_ArtifactoryFeatures_) {
		ArtifactoryFeatures = _ArtifactoryFeatures_;
	}
  function getFooterData(FooterDao, $httpBackend) {
  	FooterDao.get(true);
  	$httpBackend.flush();
  }
	beforeEach(inject(setup));

	describe('OSS', () => {
		new FooterMock().mockOss();
		beforeEach(inject(getFooterData));
		it('isDisabled should return false for OSS feature', () => {
			expect(ArtifactoryFeatures.isDisabled('Ruby')).toBeFalsy();
		});
		it('isDisabled should return true for PRO feature', () => {
			expect(ArtifactoryFeatures.isDisabled('NuGet')).toBeTruthy();
		});
		it('isDisabled should return true for ENT feature', () => {
			expect(ArtifactoryFeatures.isDisabled('highAvailability')).toBeTruthy();
		});
		it('isHidden should return true for hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('register_pro')).toBeTruthy();
		});
		it('isHidden should return false for non hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('highAvailability')).toBeFalsy();
		});
	});

	describe('PRO', () => {
		new FooterMock().mockPro()
		beforeEach(inject(getFooterData));
		it('isDisabled should return false for OSS feature', () => {
			expect(ArtifactoryFeatures.isDisabled('Ruby')).toBeFalsy();
		});
		it('isDisabled should return false for PRO feature', () => {
			expect(ArtifactoryFeatures.isDisabled('NuGet')).toBeFalsy();
		});
		it('isDisabled should return true for ENT feature', () => {
			expect(ArtifactoryFeatures.isDisabled('highAvailability')).toBeTruthy();
		});
		it('isHidden should return false for hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('register_pro')).toBeFalsy();
		});
		it('isHidden should return false for non hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('highAvailability')).toBeFalsy();
		});
	});

	describe('ENT', () => {
		new FooterMock().mockEnt();
		beforeEach(inject(getFooterData));
		it('isDisabled should return false for OSS feature', () => {
			expect(ArtifactoryFeatures.isDisabled('Ruby')).toBeFalsy();
		});
		it('isDisabled should return false for PRO feature', () => {
			expect(ArtifactoryFeatures.isDisabled('NuGet')).toBeFalsy();
		});
		it('isDisabled should return false for ENT feature', () => {
			expect(ArtifactoryFeatures.isDisabled('highAvailability')).toBeFalsy();
		});
		it('isHidden should return false for hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('register_pro')).toBeFalsy();
		});
		it('isHidden should return false for non hidden OSS feature', () => {
			expect(ArtifactoryFeatures.isHidden('highAvailability')).toBeFalsy();
		});
	});

	describe('offline', () => {
		new FooterMock();
		beforeEach(inject(getFooterData));
		it('isHidden should return false for hidden AOL feature', () => {
			expect(ArtifactoryFeatures.isHidden('backups')).toBeFalsy();
		});
		it('isHidden should return false for non-hidden AOL feature', () => {
			expect(ArtifactoryFeatures.isHidden('properties')).toBeFalsy();
		});
	});

	describe('online', () => {
		new FooterMock().mockAol();
		beforeEach(inject(getFooterData));
		it('isHidden should return true for hidden AOL feature', () => {
			expect(ArtifactoryFeatures.isHidden('backups')).toBeTruthy();
		});
		it('isHidden should return false for non-hidden AOL feature', () => {
			expect(ArtifactoryFeatures.isHidden('properties')).toBeFalsy();
		});
	});

	describe('getAllowedLicense', () => {
		new FooterMock();
		beforeEach(inject(getFooterData));
		it ('should return OSS', () => {
			expect(ArtifactoryFeatures.getAllowedLicense('Ruby')).toEqual("OSS");
		});
		it ('should return PRO', () => {
			expect(ArtifactoryFeatures.getAllowedLicense('NuGet')).toEqual("PRO");
		});
		it ('should return ENT', () => {
			expect(ArtifactoryFeatures.getAllowedLicense('highAvailability')).toEqual("ENT");
		});
	});
});