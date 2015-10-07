import {ArtifactoryDao} from '../artifactory_dao';

export function GeneralConfigDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.GENERAL_CONFIG + "/:param")
        .setCustomActions({
            'deleteLogo': {
                method: 'DELETE',
                params: {param: 'logo'}
            },
                getData: {
                    method: 'GET',
                    path: RESOURCE.GENERAL_CONFIG + "/data"
                }
        })
        .getInstance();
}