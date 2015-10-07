import {ArtifactoryDao} from '../artifactory_dao';

export class IndexerDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.INDEXER);

        this.setCustomActions({
            'run': {
                method: 'POST',
                notifications: true
            },
            'save': {
                method: 'PUT',
                notifications: true
            }
        })
    }
}