import {ArtifactoryDao} from '../../artifactory_dao';

export class ArtifactBuildsDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setPath(RESOURCE.ARTIFACT_BUILDS);
        this.setCustomActions({
            'query': {
                isArray: false
            },
            'getJson': {
                path: RESOURCE.ARTIFACT_BUILDS + '/json',
                transformResponse: (data) => {
                    return {json: data};
                }
            }
        });
    }
}
