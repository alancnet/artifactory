import {ArtifactoryDao} from '../artifactory_dao';

export class CronTimeDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.CRON_TIME);
        this.setCustomActions({
            get: {
                "params": {
                    cron: '@cron',
                    isReplication:'@isReplication'
                }
            }
        })
    }
}
