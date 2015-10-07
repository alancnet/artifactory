import {ArtifactoryDao} from '../artifactory_dao';

export class PermissionsDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,artifactoryNotificationsInterceptor)
    {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.TARGET_PERMISSIONS + '/:action/:name');

        this.setCustomActions({
            getAll: {
                method: 'GET',
                isArray: true,
                params: {action: 'crud'}
            },
            getAllUsersAndGroups: {
                method: 'GET',
                params: {action: 'allUsersGroups'}
            },
            getPermission: {
                method: 'GET',
                params: {name: '@name', action: 'crud'}
            },
            deletePermission: {
                method: 'POST',
                params: {action:'deleteTargetPermissions'},
                notifications: true
            },
            update: {
                method: 'PUT',
                params: {name: '@name'}
            },
            create: {
                method: 'POST',
                notifications: true
            }
        });
    }
}