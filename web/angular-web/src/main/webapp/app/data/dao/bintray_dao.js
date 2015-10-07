import {ArtifactoryDao} from '../artifactory_dao';

export class BintrayDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.BINTRAY_SETTING);
        this.setCustomActions({
            'fetch': {
                notifications: true
            },
            'info': {
                path: RESOURCE.BINTRAY_SETTING + "/info",
                params: {sha1: '@sha1'}
            }
        })
    }
}