import {ArtifactoryDao} from '../../artifactory_dao';

export function ArtifactActionsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.ARTIFACT_ACTIONS + '/:action')
        .setCustomActions({
                perform: {
                    params: {action: '@action'},
                    notifications: true
                },
                performGet: {
                    method: 'GET',
                    params: {action: '@action'},
                    notifications: true
                },
                dryRun: {
                    params: {action: '@action'}
                },
                getDeleteVersions: {
                    method: 'GET',
                    path: RESOURCE.ARTIFACT_ACTIONS + '/deleteversions',
                    isArray: false,
                    notifications: true
                }
        }).getInstance();
}
