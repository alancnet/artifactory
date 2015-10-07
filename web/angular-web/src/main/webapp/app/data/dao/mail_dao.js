import {ArtifactoryDao} from '../artifactory_dao';

export class MailDao extends ArtifactoryDao{

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor){
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL+RESOURCE.MAIL);
    }
}