import {ArtifactoryDao} from '../artifactory_dao';

export function SamlDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
            .setPath(RESOURCE.SAML_CONFIG)
}


/*  OLLLLLLLLLDDDDDD - (should be removed)
export class SamlDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,ArtifactoryDaoFactory) {
        super($resource, RESOURCE,ArtifactoryDaoFactory);

        this.setUrl(RESOURCE.API_URL + RESOURCE.SAML)
    }
}
*/
