import {ArtifactoryDao} from '../artifactory_dao';

export function LdapGroupsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.LDAP_GROUPS + "/:name/:action/:username")
            .setCustomActions({
                'get':{
                    params: {name: '@name'}
                },
                'update':{
                    params: {name: '@name'}
                },
                'delete':{
                    params: {name: '@name'}
                },
                'refresh':{
                    method: 'POST',
                    isArray: true,
                    params: {name: '@name', action: 'refresh', username: '@username'},
                    notifications: true
                },
                'import':{
                    method: 'POST',
                    params: {name: '@name', action: 'import'},
                    notifications: true
                },
                'getstrategy':{
                    method: 'GET',
                    params: {name: '@name', action: 'strategy'}
                }
            })
            .getInstance();
}