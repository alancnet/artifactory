export class LicensesDao {

    constructor(RESOURCE, ArtifactoryDaoFactory) {
        return ArtifactoryDaoFactory()
            .setPath(RESOURCE.LICENSES + "/:action/:name")
            .setCustomActions({
                'getLicense': {
                    method: 'GET',
                    params: {action: 'crud',name: '@name'},
                    notifications: true,
                    isArray: true
                },
                    'getSingleLicense': {
                        method: 'GET',
                        params: {action: 'crud',name: '@name'},
                        notifications: true,
                    },
                'update': {
                        method: 'PUT',
                        params: {action: 'crud',name: '@name'},
                        notifications: true,
                },
                'create': {
                    method: 'POST',
                    params: {action: 'crud',name: '@name'},
                    notifications: true,
                },
                'delete': {
                    method: 'POST',
                    params: {action: 'deleteLicense',name: '@name'}
                }

            })
            .getInstance();
    }
}