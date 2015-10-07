export class AdminAdvancedSecurityDescriptorController {
    constructor($timeout, ArtifactoryHttpClient, ArtifactoryNotifications, RESOURCE) {
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.RESOURCE = RESOURCE;
        this.$timeout = $timeout;
        this.securityDescriptor = '';
        this.apiAccess = {};
        this._getData();
    }

    _getData() {
        this.artifactoryHttpClient.get(this.RESOURCE.SECURITY_DESCRIPTOR).then((response) => {
            this.securityDescriptor = response.data;
            this.$timeout(()=> {
                this.apiAccess.api.clearHistory();
            });
        });
    }

    save(securityXML) {
        this.artifactoryHttpClient.put(this.RESOURCE.SECURITY_DESCRIPTOR, {securityXML}).
            success((response) =>
                this.artifactoryNotifications.create(response)
        )
            .error((response) => {
                if (response.errors && response.errors.length) {
                    this.artifactoryNotifications.create(angular.fromJson(response.errors[0].message));
                }
            });
    }

    cancel() {
        this._getData();
    }
}