export function BackupDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.BACKUP + "/:key/:action")
        .setCustomActions({
            'delete': {
                params: {key: '@key'}
            },
            'get': {
                params: {key: '@key'}
            },
            'update': {
                params: {key: '@key'}
            },
            'runNow': {
                method: 'POST',
                params: {key: '@key', action: 'runnow'},
                notifications: true
            }
        })
        .getInstance();
}
