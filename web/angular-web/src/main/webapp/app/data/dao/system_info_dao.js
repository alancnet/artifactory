import {ArtifactoryDao} from '../artifactory_dao';

export class SystemInfoDao extends ArtifactoryDao{

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL+RESOURCE.SYSTEM_INFO);
    }
}



