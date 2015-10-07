import {ArtifactoryDao} from '../../artifactory_dao';

export function BuildsDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()

        .setPath(RESOURCE.BUILDS + "/:action/:subAction/:name/:number/:time/:moduleId")
        .setCustomActions({

            getData: {
                method: 'GET',
                isArray: false,
                params: {action: '@action', subAction: '@subAction',name: '@name', number: '@number', time: '@time', moduleId: '@moduleId'}
            },
            lastBuild: {
                method: 'GET',
                isArray: false,
                params: {action: 'buildInfo', name: '@name', number: '@number'}
            },
            getDataArray: {
                method: 'GET',
                isArray: true,
                params: {action: '@action', subAction: '@subAction', name: '@name', number: '@number', time: '@time', moduleId: '@moduleId'}
            },
            delete:{
                method: 'POST',
                notifications: true,
                params: {action: 'buildsDelete'}
            },
            deleteAll:{
                method: 'POST',
                notifications: true,
                params: {action: 'deleteAllBuilds'}
            },
            overrideLicenses:{
                method: 'PUT',
                params: {action: 'overrideLicenses', name: '@name', number: '@number', time: '@time'}
            },
            updateGovernanceRequest: {
                method: 'PUT',
                notifications: true,
                params: {action: 'updateGovernance', name: '@name', number: '@number', time: '@time'}
            }

        })
        .getInstance();
}