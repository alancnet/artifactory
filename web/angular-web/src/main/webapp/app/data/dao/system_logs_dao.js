export function SystemLogsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.SYSTEM_LOGS + "/:action")
        .setCustomActions({
            'getLogs': {
                method: 'GET',
                params: {action: 'initialize'}
            },
            'getLogData': {
                method: 'GET',
                params: {action: 'logData'}
            },
            'getDownloadLink': {
                method: 'GET',
                params: {action: 'downloadFile'}
            }
        })
        .getInstance();
}