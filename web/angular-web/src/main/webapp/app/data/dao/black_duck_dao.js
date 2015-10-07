import {ArtifactoryDao} from '../artifactory_dao';

export class BlackDuckDao extends ArtifactoryDao{

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.BLACK_DUCK)
    }
}