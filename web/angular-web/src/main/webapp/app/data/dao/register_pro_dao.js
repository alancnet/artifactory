export function RegisterProDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.REGISTER_PRO)
            .setCustomActions({
                'update': {
                    notifications: true
                }

            })
            .getInstance();
}