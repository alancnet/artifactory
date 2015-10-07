import {ArtifactoryDao} from '../artifactory_dao';

export class UserDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.USERS + '/:prefix/:name');

        this.setCustomActions({
            'getAll': {
                method: 'GET',
                params: {prefix: 'crud'},
                isArray: true
            },
            'getSingle': {
                method: 'GET',
                params: {name: '@name', prefix: 'crud'}
            },
            'create': {
                method: 'POST',
                notifications: true
            },
            'update': {
                method: 'PUT',
                params: {name: '@name'},
                notifications: true
            },
            'delete': {
                method: 'POST',
                params: {prefix: 'userDelete'},
                notifications: true
            },
            'getPermissions': {
                method: 'GET',
                params: {name: '@name', prefix: 'permissions'},
                isArray: true
            },
            'checkExternalStatus': {
                method: 'POST',
                params: {prefix: 'externalStatus'},
                notifications: true
            },

        })
    }
}
