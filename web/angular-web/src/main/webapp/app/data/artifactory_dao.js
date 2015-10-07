/**
 * base class for DAO's
 */
export class ArtifactoryDao {

    /**
     * inject services provided by the sub class
     * @param $resource
     * @param RESOURCE
     */
    constructor($resource, RESOURCE, artifactoryNotificationsInterceptor) {
        this.artifactoryNotificationsInterceptor = artifactoryNotificationsInterceptor;
        this.RESOURCE = RESOURCE;
        this.$resource = $resource;
        this._url = null;
        this._prototype = null;

        /**
         * default custom actions
         * @type {{update: {method: string, interceptor: *}, save: {method: string, interceptor: *}, delete: {method: string, interceptor: *}}}
         * @private
         */
        this.setDefaults({});
        this.setCustomActions({
            'update': {
                method: 'PUT',
                notifications: true
            },
            'delete': {
                method: 'DELETE',
                notifications: true
            },
            'fetch': { // POST which is actually getting data
                method: 'POST',
                notifications: false
            },
            'save': {
                method: 'POST',
                notifications: true
            }
        });
    }

    /**
     * full url for the resource, include params
     * @param {string} url
     */
    setUrl(url) {
        this._url = url;
        return this;
    }

    /**
     * relative path for the resource, include params
     * @param {string} path
     */
    setPath(path) {
        this._url = this.RESOURCE.API_URL + path;
        return this;
    }

    /**
     * set defaults method for custom actions
     * @param {object} options - Object of default properties. Possible keys and values:
     ** @param {string} method - GET, POST, PUT, DELETE, PATCH, etc.
     */
    setDefaults(options) {
        this._defaults = this._defaults || {};
        angular.extend(this._defaults, options);
        return this;
    }

    /**
     * extend the default actions object
     * @param {object} actionsObject
     */
    setCustomActions(actionsObject) {
        this._customActions = this._customActions || {};
        Object.keys(actionsObject).forEach((action) => {
            let actionParams = actionsObject[action];
            if (!actionParams.method && this._defaults.method) {
                actionParams.method = this._defaults.method;
            }
            if (actionParams.path) {
                actionParams.url = this.RESOURCE.API_URL + actionParams.path;
                delete actionParams.path;
            }
            if (angular.isDefined(actionParams.notifications) && actionParams.notifications) {
                if (actionParams.notifications) actionParams.interceptor = this.artifactoryNotificationsInterceptor;
            }
            if (angular.isDefined(actionParams.notifications)) {
                delete actionParams.notifications;
            }
            if (this._customActions[action]) {
                angular.extend(this._customActions[action], actionParams);
            } else {
                this._customActions[action] = angular.copy(actionParams);
            }
        });
        return this;
    }

    /**
     * extend the resources prototype
     * @param {object} prototype
     */
    extendPrototype(prototype) {
        this._prototype = prototype;
        return this;
    }

    /**
     * supply a transform function
     * @callback {function} transform function
     */
    transformResponse(callback) {
        this.transformResponse = callback;
        return this;
    }

    /**
     *returns an instance of the resource object
     * @returns {Object|*} $resource
     */
    getInstance() {
        var result = this.$resource(this._url, null, this._customActions);
        if (this._prototype) {
            angular.extend(result.prototype, this._prototype);
        }
        return result;
    }
}

export function ArtifactoryDaoFactory($resource, RESOURCE, artifactoryNotificationsInterceptor) {
    return function () {
        return new ArtifactoryDao($resource, RESOURCE, artifactoryNotificationsInterceptor);
    }
}