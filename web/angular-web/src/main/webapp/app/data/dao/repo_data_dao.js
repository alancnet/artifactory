export function RepoDataDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.REPO_DATA)
            .setCustomActions({
                getAllForPerms: {
                    method: 'GET'
                },
                getAll: {
                    method: 'GET',
                    isArray: true,
                    path: RESOURCE.REPO_DATA + "?user=true"
                },
                getForSearch: {
                    method: 'GET',
                    path: RESOURCE.REPO_DATA + "?search=true"
                }
            })
            .getInstance();
}