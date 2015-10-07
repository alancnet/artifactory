import {ArtifactoryDao} from '../artifactory_dao';

export function ExportDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.EXPORT + '/:action')
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