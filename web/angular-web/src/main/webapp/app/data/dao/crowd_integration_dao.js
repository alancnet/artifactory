export function CrowdIntegrationDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.CROWD)
            .setCustomActions({
                'test': {
                    method: 'POST',
                    path: RESOURCE.CROWD + "/test",
                    notifications: true
                },
                'refresh': {
                    method: 'POST',
                    path: RESOURCE.CROWD + "/refresh/:name",
                    params: {name: '@name'},
                    notifications: true
                },
                'import': {
                    method: 'POST',
                    path: RESOURCE.CROWD + "/import",
                    notifications: true
                }
            })
            .getInstance();
}