import {ArtifactoryDao} from '../artifactory_dao';

export function ImportDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.IMPORT + '/:action')
            .setCustomActions({
                'save': {
                    method: 'POST',
                    params: {
                        action: '@action'
                    },
                    notifications: true
                }
            })
            .getInstance();
}