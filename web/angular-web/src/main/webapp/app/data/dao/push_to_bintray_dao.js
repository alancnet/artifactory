import {ArtifactoryDao} from '../artifactory_dao';

export function PushToBintrayDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setPath(RESOURCE.PUSH_TO_BINTRAY + "/:param1/:param2/:param3/:param4")
        .setCustomActions({
            'getBuildRepos': {
                method: 'GET'
            },
            'getBuildPacks': {
                method: 'GET',
                params: {param1: 'build', param2: 'pkg'}
            },
            'getBuildVersions': {
                method: 'GET',
                params: {param1: 'build', param2: 'versions'}
            },
            'pushBuildToBintray': {
                method: 'POST',
                notifications: true,
                params: {param1: 'build', param2: '@buildName', param3: '@buildNumber', param4: '@buildTime'}
            },
            'getArtifactData': {
                method: 'GET',
                params: {param1: 'artifact'}
            },
            'pushArtifactToBintray': {
                method: 'POST',
                notifications: true,
                params: {param1: 'artifact', param2: '@repoKey', param3: '@path'}
            }
        })
        .getInstance();
}