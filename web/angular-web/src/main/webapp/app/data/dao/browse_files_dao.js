import {ArtifactoryDao} from '../artifactory_dao';

export class BrowseFilesDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.BROWSE_FILESYSTEM);
        this.setCustomActions({
            'query': {
                method: 'GET',
                params: {path: '@path'}
            }
        });
    }
}