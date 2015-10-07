import {ArtifactoryDao} from '../artifactory_dao';

export class DateFormatDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.DATE_FORMAT);
        this.setCustomActions({
            get: {
                "params": {
                    dateformat: '@dateformat'
                }
            }
        })
    }
}
