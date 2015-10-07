import {ArtifactoryDao} from '../artifactory_dao';

export class PasswordsEncryptionDao extends ArtifactoryDao {

    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        super($resource, RESOURCE,artifactoryNotificationsInterceptor);

        this.setUrl(RESOURCE.API_URL + RESOURCE.CRYPTO + "/:action");

        this.setCustomActions({
            'encrypt': {
                method: 'POST',
                params: {action: 'encrypt'},
                notifications: true
            },

            'decrypt': {
                method: 'POST',
                params: {action: 'decrypt'},
                notifications: true
            }
        })
    }
}