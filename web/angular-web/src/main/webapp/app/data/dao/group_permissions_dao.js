import {ArtifactoryDao} from '../artifactory_dao';

export class GroupPermissionsDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.GROUP_PERMISSION);

        this.setCustomActions({
            'get': {
                method: 'POST',
                isArray: true
            }
        });
    }
}