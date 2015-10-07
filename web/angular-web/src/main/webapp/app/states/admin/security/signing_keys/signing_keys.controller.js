import EVENTS     from '../../../../constants/artifacts_events.constants';
import API from '../../../../constants/api.constants';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class SigningKeysController {

    constructor($timeout, FileUploader, ArtifactoryEventBus, SigningKeysDao, KeystoreDao, ArtifactoryNotifications) {
        this.$timeout = $timeout;
        this.signingKeysDao = SigningKeysDao;
        this.keystoreDao = KeystoreDao;
        this.keyStore = {};
        this.FileUploader = FileUploader;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.TOOLTIP = TOOLTIP.admin.security.signingKeys;
        this.publicKeyValue = 'No public key installed';
        this.privateKeyValue = 'No private key installed';
        this.initSigningKeys();
    }

    initSigningKeys() {

        this.getSigningKeysData();
        this.keyPairNames = [];
        this.keystoreFileUploaded = false;
        this.getKeyStoreData();
        //-----public key-----//
        this.uploaderPublicKey = new this.FileUploader();
        this.uploaderPublicKey.onSuccessItem = this.onUploadPublicKeySuccess.bind(this);
        this.uploaderPublicKey.url = `${API.API_URL}/signingkeys/install?public=true`;
        this.uploaderPublicKey.removeAfterUpload = true;
        //-----private key-----//
        this.uploaderPrivateKey = new this.FileUploader();
        this.uploaderPrivateKey.url = `${API.API_URL}/signingkeys/install?public=false`;
        this.uploaderPrivateKey.onSuccessItem = this.onUploadPrivateKeySuccess.bind(this);
        this.uploaderPrivateKey.removeAfterUpload = true;
        //------key pair-----//
        this.uploaderKeyStore = new this.FileUploader();
        this.uploaderKeyStore.onSuccessItem = this.onUploadKeyStoreSuccess.bind(this);
        this.uploaderKeyStore.onErrorItem = this.onUploadKeyStoreFail.bind(this);
        this.uploaderKeyStore.url = `${API.API_URL}/keystore/upload?pass=`;
        this.uploaderKeyStore.removeAfterUpload = true;
    }

    getSigningKeysData() {
        this.signingKeysDao.get().$promise.then((result) => {
            this.publicKeyInstalled = result.publicKeyInstalled;
            this.privateKeyInstalled = result.privateKeyInstalled;
            this.publicKeyValue = result.publicKeyInstalled ? 'Public key is installed' : 'No public key installed';
            this.privateKeyValue = result.privateKeyInstalled ? 'Private key is installed' : 'No private key installed';
            this.publicKeyLink = result.publicKeyLink;
            this.passPhrase = result.passPhrase;
        });
    }

    getKeyStoreData() {
        this.keystoreDao.get().$promise.then((keyStore) => {
            this.keyStoreExist = keyStore.keyStoreExist;
            _.map(keyStore.keyStorePairNames, (keypairName) => {
                this.keyPairNames.push(keypairName);
            })
        });
    }

    onUploadPublicKeySuccess(fileDetails, response) {
        this.getSigningKeysData();
        this.artifactoryNotifications.create(response.feedbackMsg);
    }

    onUploadPrivateKeySuccess(fileDetails, response) {
        this.getSigningKeysData();
        this.artifactoryNotifications.create(response.feedbackMsg);
    }

    upload(type) {
        if (type === 'public') {
            this.uploaderPublicKey.queue[0].upload();
        }
        if (type === 'private') {
            this.uploaderPrivateKey.queue[0].upload();
        }
        if (type === 'keyStore') {
            this.uploaderKeyStore.queue[0].url = `${API.API_URL}/keystore/upload?pass=${this.keyPair.keyStorePassword}`;
            this.uploaderKeyStore.queue[0].upload();
        }
    }

    removeKey(isPublic) {
        this.signingKeysDao.delete({public: isPublic}).$promise.then((result) => this.getSigningKeysData());
    }

    verifyPhrase(shouldNotify = true) {
        //this.signingKeysDao.setNotification('post', shouldNotify);
        let method = shouldNotify ? 'post' : 'postWithoutNotifications';
        if (this.signingKeysDao[method])
            return this.signingKeysDao[method]({action: 'verify', passPhrase: this.passPhrase}).$promise;
    }

    updatePhrase() {
        let verifyPromise = this.verifyPhrase(false);
        if (verifyPromise) {
            verifyPromise
                .then(() => {
                    this.signingKeysDao.put({action: 'update', passPhrase: this.passPhrase});
                })
                .catch((response) => this.artifactoryNotifications.create(response.data));
            ;
        }
    }

    checkMatchingPasswords() {
        this.$timeout(() => {
            if (this.signingKeysForm.password.$valid && this.signingKeysForm.repeatPassword.$valid) {
                this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION);
            }
        });
    }

    updatePassword() {
        this.keystoreDao.updatePassword({action: 'updatePass'}, {password: this.user.password}).$promise
            .then(() => {
                this.keyStoreExist = true;
            });
    }

    removeKeyStore() {
        this.keystoreDao.removeKeystore({action: 'password'}).$promise
            .then(() => {
                //_.forEach(this.keyPairNames, this.removeKeypair, this);
                this.keyStoreExist = false;
                this.keyPairNames = [];
                this.keyPairName = '';
                this.user.password = '';
                this.repeatPassword = '';
            })
    }

    onUploadKeyStoreSuccess(fileDetails, keyStore) {
        this.keystoreFileUploaded = true;
        this.keyStore = keyStore;
        this.alias = keyStore.alias;
        this.keyPair.keyStorePassword = '';
        this.artifactoryNotifications.create(keyStore.feedbackMsg);
    }

    onUploadKeyStoreFail(fileDetails, response) {
        this.artifactoryNotifications.create(response);
    }

    saveKeypair() {
        this.keystoreDao.save({action: 'add'}, this.keyStore).$promise
            .then((response) => {
                this.keyPairNames.push(this.keyStore.keyPairName);
                this.keyStore.keyPairName = '';
                this.keyStore.privateKeyPassword = '';
                this.alias = '';
                this.aliases = [];
            });
        //.finally(() => this.keystoreFileUploaded = false);
    }

;

    removeKeypair() {
        this.keystoreDao.removeKeypair({name: this.keyPairName}).$promise.then((response) => {
            _.pull(this.keyPairNames, this.keyPairName);
            this.keyPairName = '';
        }).catch((response) => {
            if (response.error) {
                let keyPairNames = _.words(response.error);
                _.pull(this.keyPairNames, keyPairNames.pop());
            }
        });
    }

    cancelKeypairUpload() {
        //this.keystoreFileUploaded = false;
        this.keyStore.keyPairName = '';
        this.keyStore.privateKeyPassword = '';
    }

    canUpdatePassword() {
        return this.signingKeysForm.password.$valid && this.signingKeysForm.repeatPassword.$valid;
    }

    canUploadKeystore() {
        return this.keyStoreExist &&
            this.signingKeysForm.keyStorePassword.$valid &&
            this.uploaderKeyStore.queue.length;
    }

    canUploadDebianKey(uploader) {
        return this[uploader].queue.length;
    }

    canRemoveKeyPairs() {
        return this.keyStoreExist && this.keyPairNames.length && this.keyPairName;
    }

    canUpdatePhrase() {
        return this.publicKeyInstalled && this.privateKeyInstalled && this.passPhrase;
    }
}
