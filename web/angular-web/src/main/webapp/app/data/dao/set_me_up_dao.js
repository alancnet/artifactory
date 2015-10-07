import {ArtifactoryDao} from '../artifactory_dao';

export function SetMeUpDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.SET_ME_UP)
        .setCustomActions({
            fetch: {
                notifications: true
            },
            maven : {
                path : RESOURCE.SET_ME_UP_MAVEN
            },
            gradle : {
                path : RESOURCE.SET_ME_UP_GRADLE
            },
            ivy : {
                path : RESOURCE.SET_ME_UP_IVY
            },
            maven_distribution : {
              path : RESOURCE.SET_ME_UP_MAVEN_DISTRIBUTION,
              method : "GET"
            },
            maven_snippet : {
                path : RESOURCE.SET_ME_UP_MAVEN_SNIPPET,
                method : "POST"
            },
            gradle_snippet : {
                path : RESOURCE.SET_ME_UP_GRADLE_SNIPPET,
                method : "POST"
            },
            ivy_snippet : {
                path : RESOURCE.SET_ME_UP_IVY_SNIPPET,
                method : "POST"
            }
        }).getInstance();
}