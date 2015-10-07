import {ArtifactoryDao} from '../../artifactory_dao';

export class ArtifactPropertyDao extends ArtifactoryDao {

    constructor(RESOURCE, $resource, artifactoryNotificationsInterceptor) {
        super($resource,RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.ARTIFACT_PROPERTIES +"/:name");
        this.setCustomActions({
            'query': {
                isArray: false
            },
            'get': {
                params: {name: '@name'}
            },
            'update': {
                params: {name: '@name'}
            },
            'delete':{
                params: {name: '@name'}
            },
            'deleteBatch': {
                url: RESOURCE.API_URL+'/deleteproperties',
                method: 'POST',
                notifications:true
            }
        });
    }
}
