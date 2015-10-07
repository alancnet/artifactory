/**
 * wrapper around the HTML5 local storage API.
 * support JSON serialization de-serialization.
 *
 */

let storage;
export class ArtifactoryStorage {

    constructor($window) {
        storage = $window.localStorage;
    }

    setItem(key, item) {
        try {
            storage.setItem(key, JSON.stringify(item));
            return this.getItem(key);
        }
        catch (e) {
            console.log(e)
        }
    }

    getItem(key, defaultValue = null) {
        try {
            let itemStr = storage.getItem(key);
            if (itemStr) {
                return JSON.parse(itemStr);
            }
            else {
                return defaultValue;
            }
        }
        catch (e) {
            console.log(e)
        }
    }

    removeItem(key) {
        storage.removeItem(key);
    }

    isLocalStorageNameSupported() {
        let testKey = 'test', storage = window.sessionStorage;
        try {
            storage.setItem(testKey, '1');
            storage.removeItem(testKey);
            return localStorageName in win && win[localStorageName];
        }
        catch (error) {
            return false;
        }
    }
}