import {ArtifactoryDao} from '../../artifactory_dao';

export function ArtifactSearchDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.ARTIFACT_SEARCH + '/:search/:action')
            .setCustomActions({
                'fetch': {
                    params: {search: '@search'},
                    notifications:true
                },
                'get': {
                    method: 'GET',
                    isArray: true,
                    params: {
                        action: '@action',
                        search: '@search'
                    },
                    notifications:true
                },
                'delete': {
                    method: 'POST',
                    params: {search: 'deleteArtifact'}
                }
            })
            .getInstance();
}