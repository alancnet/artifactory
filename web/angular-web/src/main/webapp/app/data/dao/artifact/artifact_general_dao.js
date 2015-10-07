export function ArtifactGeneralDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.ARTIFACT_GENERAL)
        .setCustomActions({
            'bintray': {
                method: 'POST',
                path: RESOURCE.ARTIFACT_GENERAL_BINTRAY,
                params: {sha1: '@sha1', $no_spinner: true}
            },
            artifactsCount: {
                method: 'POST',
                path: RESOURCE.ARTIFACT_GENERAL + "/artifactsCount",
                params: {$no_spinner: true},
                notifications: false
            }
        }).getInstance();
}