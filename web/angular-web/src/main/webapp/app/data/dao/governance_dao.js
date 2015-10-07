import {ArtifactoryDao} from '../artifactory_dao';

export function GovernanceDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'POST'})
        .setPath(RESOURCE.ARTIFACT_GOVERNANCE)
        .getInstance();
}