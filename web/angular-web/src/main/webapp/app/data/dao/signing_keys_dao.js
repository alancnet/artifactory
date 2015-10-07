import {ArtifactoryDao} from '../artifactory_dao';

export function SigningKeysDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.SIGNINGKEYS + "/:action")
        .setCustomActions({
            'delete': {
                method: 'DELETE',
                params: {'public': '@public'}
            },
            post: {
                method: 'POST',
                params: {'action': '@action'},
                notifications: true
            },
            postWithoutNotifications: {
                method: 'POST',
                params: {action: '@action'},
                notifications: false
            },
            put: {
                method: 'PUT',
                params: {'action': '@action'},
                notifications: true
            }
        })
        .getInstance();
}