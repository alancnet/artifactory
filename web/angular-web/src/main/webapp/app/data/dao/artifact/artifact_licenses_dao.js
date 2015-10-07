import {ArtifactoryDao} from '../../artifactory_dao';

export function ArtifactLicensesDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.GENERAL_TAB_LICENSES + '/:action')
        .setCustomActions({
            'getLicenses': {
                method: 'GET',
                isArray: true
            },
            setLicenses: {
                method: 'PUT',
                isArray: false,
                path: RESOURCE.GENERAL_TAB_LICENSES + '/setLicensesOnPath',
                notifications: true
            },
            scanArtifact: {
                method: 'GET',
                isArray: true,
                path: RESOURCE.GENERAL_TAB_LICENSES + '/scanArtifact',
                notifications: true
            },
            queryCodeCenter: {
                method: 'POST',
                params: {
                    repoKey: '@repoKey',
                    path: '@path'
                },
                path: RESOURCE.GENERAL_TAB_LICENSES + '/queryCodeCenter',
                notifications: true
            },
            getArchiveLicenseFile: {
                method: 'GET',
                path: RESOURCE.GENERAL_TAB_LICENSES + '/getArchiveLicenseFile',
                transformResponse:(data)=>{ return {data: data} }
            }
        })
        .getInstance();
}