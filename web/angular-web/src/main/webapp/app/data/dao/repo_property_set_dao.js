import {ArtifactoryDao} from '../artifactory_dao';

export class RepoPropertySetDao extends ArtifactoryDao {

    constructor($resource, RESOURCE,ArtifactoryDaoFactory) {
        super($resource, RESOURCE,ArtifactoryDaoFactory);

        this.setUrl(RESOURCE.API_URL + RESOURCE.REPO_PROPERTY_SET)
    }
}