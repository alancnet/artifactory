import {ArtifactoryDao} from '../artifactory_dao';

export class PredefineDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,ArtifactoryDaoFactory) {
        super($resource, RESOURCE,ArtifactoryDaoFactory);

        this.setUrl(RESOURCE.API_URL + RESOURCE.PREDEFINE_VALUES + "/:name");
        this.setCustomActions({
            'get': {
                params: {name: "@name"}
            }
        })
    }
}