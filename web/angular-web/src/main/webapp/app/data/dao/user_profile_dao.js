import {ArtifactoryDao} from '../artifactory_dao';

export function UserProfileDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.USER_PROFILE)
        .setCustomActions({
            fetch: {
                notifications: true
            }
        }).getInstance();
}
