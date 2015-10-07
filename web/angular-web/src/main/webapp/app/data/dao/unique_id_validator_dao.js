import {ArtifactoryDao} from '../artifactory_dao';

export class UniqueIdValidatorDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.UNIQUE_ID_VALIDATOR);
        this.setCustomActions({
            get: {
                "params": {
                    id: '@id'
                }
            }
        })
    }
}
