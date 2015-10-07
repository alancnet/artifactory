export class AdminSecurityGeneralDao {
    constructor(ArtifactoryDaoFactory, RESOURCE) {
	    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.SECURITY_CONFIG)
            .getInstance();
    }
}