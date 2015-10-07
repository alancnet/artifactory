import {ArtifactoryDao} from '../artifactory_dao';

export function KeystoreDao(RESOURCE, ArtifactoryDaoFactory) {
    return ArtifactoryDaoFactory()
        .setDefaults({method: 'GET'})
        .setPath(RESOURCE.KEYSTORE+"/:action")
        .setCustomActions({
            'delete':{
                method: 'DELETE',
                params:{'public':'@public'}
            },
            post:{
                method: 'POST',
                params:{'action':'@action'},
                notifications: true
            },
            updatePassword: {
                method: 'PUT',
                notifications: true
            },
            removeKeystore: {
                method: 'DELETE',
                notifications: true
            },
            removeKeypair: {
                method: 'DELETE',
                params: {'name': '@name'},
                notifications: true
            }
        })
        .getInstance();
}