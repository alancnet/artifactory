import {ArtifactoryDao} from '../artifactory_dao';

export function ChecksumsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.CHECKSUMS + "/:action")
        .setCustomActions({
            'fix': {
                method: 'POST',
                params: {action: 'fix'},
                notifications: true
            }
        })
        .getInstance();
}