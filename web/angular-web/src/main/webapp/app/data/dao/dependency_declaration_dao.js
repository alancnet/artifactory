import {ArtifactoryDao} from '../artifactory_dao';

export function DependencyDeclarationDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setDefaults({method: 'POST'})
            .setPath(RESOURCE.DEPENDENCY_DECLARATION)
            .getInstance();
}