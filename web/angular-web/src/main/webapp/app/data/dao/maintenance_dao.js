import {ArtifactoryDao} from '../artifactory_dao';

export function MaintenanceDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.MAINTENANCE + '/:module')
            .setCustomActions({
                perform: {
                    params: {module: '@module'},
                    notifications: true
                }
            }).getInstance();
}
