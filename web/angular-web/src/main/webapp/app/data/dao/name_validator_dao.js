import {ArtifactoryDao} from '../artifactory_dao';

export class NameValidatorDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.NAME_VALIDATOR);
        this.setCustomActions({
            get: {
                "params": {
                    name: '@name'
                }
            }
        })
    }
}
