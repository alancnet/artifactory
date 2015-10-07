import {ArtifactoryDao} from '../../artifactory_dao';

export function ArtifactWatchesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.ARTIFACT_WATCHES + "/:name")
            .setCustomActions({
                "delete": {
                    method: 'POST',
                    path: RESOURCE.ARTIFACT_WATCHES + "/remove"
                },
                status: {
                    path: RESOURCE.ARTIFACT_WATCHES + "/status"
                }
            })
            .getInstance();
}

