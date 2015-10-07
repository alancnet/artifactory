const VERSION_INFO_KEY = 'VERSION_INFO';
export class FooterDao {
	constructor(RESOURCE, ArtifactoryDaoFactory, ArtifactoryStorage) {
		this.storage = ArtifactoryStorage;
    	this._resource = ArtifactoryDaoFactory()
            .setPath(RESOURCE.FOOTER)
            .getInstance();
    }

    get(force = false) {
        if (!this.cached || force) {
            this.cached = this._resource.get().$promise
                    .then(info => this._info = info);
        }
        return this.cached;
    }

    getInfo() {
        return this._info;
    }
}
