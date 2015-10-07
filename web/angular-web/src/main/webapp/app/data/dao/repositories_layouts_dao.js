export function RepositoriesLayoutsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.REPOSITORIES_LAYOUTS + '/:action/:name')
        .setCustomActions({
            getLayouts: {
                method: 'GET',
                isArray: true
            },
            getLayoutData: {
                method: 'GET',
                params: {name: '@layoutName'}
            },
            deleteLayout: {
                method: 'DELETE',
                params: {name: '@layoutName'},
                notifications: true
            },
            testArtifactPath: {
                method: 'POST',
                params: {action: 'testArtPath'}
            },
            resolveRegex: {
                method: 'POST',
                params: {action: 'resolveRegex'}
            }

        })
        .getInstance();
}
