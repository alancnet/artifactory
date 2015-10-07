import {ArtifactoryDao} from '../artifactory_dao';

export function LdapDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.LDAP + "/:action/:key")
            .setCustomActions({
                'save':{
                    method: 'POST'
                },
                'get':{
                    method: 'GET',
                    params: {key: '@key'}
                },
                'update':{
                    method: 'PUT',
                    params: {key: '@key'}
                },
                'delete':{
                    method: 'DELETE',
                    params: {key: '@key'}
                },
                'test':{
                    method: 'POST',
                    params: {key: '@key', action: 'test'},
                    notifications: true
                },
                'reorder':{
                    method: 'POST',
                    path: RESOURCE.LDAP + "/reorder"
                }
            })
            .getInstance();
}