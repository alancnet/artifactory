export function HaDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.HIGH_AVAILABILITY + '/:id')
            .setCustomActions({
            	'delete': {
            		params: {id: '@id'}
				}
            })
            .getInstance();
}