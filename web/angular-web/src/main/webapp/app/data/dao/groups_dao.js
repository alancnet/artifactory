import {ArtifactoryDao} from '../artifactory_dao';

export class GroupsDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.GROUPS + '/:prefix/:name');

        this.setCustomActions({

            'getAll': {
                method: 'GET',
                isArray: true,
                params: {flag: true, prefix: 'crud'}
            },
            'getSingle': {
                method: 'GET',
                params: {prefix: 'crud', name: '@name'}
            },
            'update': {
                params: {name: '@name'}
            },
            'create': {
                method: 'POST',
                notifications: true
            },
            'delete': {
                method: 'POST',
                params: {prefix: 'groupsDelete'}
            }
        });
    }
}

