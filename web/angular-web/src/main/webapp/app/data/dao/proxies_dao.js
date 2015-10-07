export function ProxiesDao(RESOURCE, ArtifactoryDaoFactory) {
	return ArtifactoryDaoFactory()
    	.setPath(RESOURCE.PROXIES + "/:prefix/:key")
        .setCustomActions({
            'delete': {
                method : 'POST',
                params: {prefix: 'deleteProxies'}
            },
            'update': {
                method : 'PUT',
                params :{prefix: 'crud', key :'@key'}
            },
            'get': {
                method : 'GET',
                params :{prefix: 'crud', key :'@key'},
                isArray: true
            },
                'getSingleProxy': {
                    method: 'GET',
                    params: {prefix: 'crud', key: '@key'}
            }
        })
        .getInstance();
}
