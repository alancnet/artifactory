import {ArtifactoryDao} from '../artifactory_dao';

export class ConfigDescriptorDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.CONFIG_DESCRIPTOR);
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
