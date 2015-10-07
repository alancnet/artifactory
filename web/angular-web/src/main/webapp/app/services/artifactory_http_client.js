/**
 * wrapper around angular $http service
 */
import RC from '../constants/api.constants'
export class ArtifactoryHttpClient {

    constructor($http, RESOURCE) {
        this.http = $http;
        this.baseUrl = RESOURCE.API_URL;
        this.config = {
            headers: {'Content-Type': 'application/json'}
        };
    }

    post(api, data, config = {}) {
        return this.http.post(this.baseUrl + api, data, angular.extend(this.config, config));
    }

    put(api, data, config = {}) {
        return this.http.put(this.baseUrl + api, data, angular.extend(this.config, config));
    }

    get(api, config = {}) {
        return this.http.get(this.baseUrl + api, angular.extend(this.config, config));
    }
}