class FooterMock {
  constructor() {
    this.mockData = {
      isAol: false,
      versionID: 'OSS'
    };
    beforeEach(inject(($httpBackend, RESOURCE) => {
      $httpBackend.whenGET(RESOURCE.API_URL + RESOURCE.FOOTER).respond(() => {
        return [200, this.mockData];
      });
    }));
  }

  mockOss() {
    this.mockData.versionID = 'OSS';
    return this;
  };
  mockPro() {
    this.mockData.versionID = 'PRO';
    return this;
  };
  mockEnt() {
    this.mockData.versionID = 'ENT';
    return this;
  };
  mockAol() {
    this.mockData.isAol = true;
    return this;
  };
}

export default FooterMock;