import {ArtifactoryDao} from '../artifactory_dao';

export class XmlNameDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE, artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.XML_NAME_VALIDATOR);
        this.setCustomActions({
            get: {
                "params": {
                    xmlName: '@xmlName'
                }
            }
        })
    }
}
