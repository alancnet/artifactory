export function StashResultsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'GET'})
            .setPath(RESOURCE.STASH_RESULTS)
            .setCustomActions({
                'get': {
                    method: 'GET',
                    isArray: true
                },
                'save': {
                    method: 'POST',
                    notifications: true
                },
                'delete': {
                    method: 'DELETE'
                },
                'add': {
                    path: RESOURCE.STASH_RESULTS + "/add",
                    notifications: true,
                    method: 'POST'
                },
                'subtract': {
                    path: RESOURCE.STASH_RESULTS + "/subtract",
                    notifications: true,
                    method: 'POST'
                },
                'intersect': {
                    path: RESOURCE.STASH_RESULTS + "/intersect",
                    notifications: true,
                    method: 'POST'
                },
                'export': {
                    path: RESOURCE.STASH_RESULTS + "/export",
                    notifications: true,
                    method: 'POST'
                },
                'discard': {
                    path: RESOURCE.STASH_RESULTS + "/discard",
                    notifications: true,
                    method: 'POST'
                },
                'copy': {
                    path: RESOURCE.STASH_RESULTS + "/copy",
                    notifications: true,
                    method: 'POST'
                },
                'move': {
                    path: RESOURCE.STASH_RESULTS + "/move",
                    notifications: true,
                    method: 'POST'
                },
                'silentCopy': {
                    path: RESOURCE.STASH_RESULTS + "/copy",
                    method: 'POST'
                },
                'silentMove': {
                    path: RESOURCE.STASH_RESULTS + "/move",
                    method: 'POST'
                }
            })
            .getInstance();
}