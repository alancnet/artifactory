import {ArtifactoryDao} from '../artifactory_dao';

export class SecurityDescriptorDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.SECURITY_DESCRIPTOR);

        this.setCustomActions({
            'get': {
                method: 'GET',
                notifications: true
            },
            'update': {
                method: 'PUT',
                notifications: true
            }
        });
    }
}