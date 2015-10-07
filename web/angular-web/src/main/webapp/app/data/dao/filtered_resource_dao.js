import {ArtifactoryDao} from '../artifactory_dao';

export function FilteredResourceDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.FILTERED_RESOURCE)
        .setCustomActions({
            'setFiltered': {
                method: 'POST'
            }
        })
        .getInstance();
}