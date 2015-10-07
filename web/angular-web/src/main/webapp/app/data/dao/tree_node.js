const ARCHIVE_MARKER = '!';

export function TreeNodeFactory($q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao, ArtifactActionsDao) {
    return function(data) {
        return new TreeNode(data, $q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao, ArtifactActionsDao)
    }
}

class TreeNode {
    constructor(data, $q, $injector, RESOURCE, ArtifactoryHttpClient, ArtifactWatchesDao, ArtifactActionsDao) {
        this.$q = $q;
        this.treeBrowserDao = $injector.get('TreeBrowserDao');
        this.RESOURCE = RESOURCE;
        this.artifactoryHttpClient = ArtifactoryHttpClient;
        this.artifactWatchesDao = ArtifactWatchesDao;
        this.artifactActionsDao = ArtifactActionsDao;

        // Wrap the data
        angular.extend(this, data);

        if (this.children) {
            this.hasChild = true;
        }
        this._initIconType();
        this._initFullpath();

        this.className = 'TreeNode';
    }
    _initIconType() {
        if (!this.icon && this.compacted && this.isLocalFolder()) {
            this.iconType = 'compactedFolder';
        }
        else if (this.isRepo()) {
            switch(this.repoType) {
                case 'virtual':
                    this.iconType = 'virtualRepository';
                    break;
                case 'remote':
                    this.iconType = 'remoteRepository';
                    break;
                case 'cached':
                    this.iconType = 'cachedRepository';
                    break;
                default:
                    this.iconType = 'localRepository';
            }
        }
        else {
            this.iconType = this.mimeType || this.icon || this.type;
        }
    }

    _initFullpath() {
        if (this.path == '') {
            this.fullpath = this.repoKey;
        }
        else {
            let path = this.path;
            if (this.archivePath) {
                path = path.replace(this.archivePath, this.archivePath + ARCHIVE_MARKER);
            }
            this.fullpath = this.repoKey + "/" + path;
        }
    }

    getRoot() {
        return this.parent ? this.parent.getRoot() : this;
    }

    findNodeByPath(path, startIndex, includeArchives = true) {
        if (startIndex === path.length) return this;
        if (this.isArchive() && !includeArchives) return this;
        // Find child:
        return this.getChildren().then((children) => {
            while(startIndex != path.length) {
                startIndex++;
                let partialPath = path.slice(0, startIndex).join('/');
                // TODO: remove second condition after Chen fixes server. Currently sometimes server returns path that ends with '/'
                let child = _.findWhere(children, {path: partialPath}) || _.findWhere(children, {path: partialPath + '/'});
                if (child) return child;
            }
        })
        .then((child) => {
        // Recursively look for rest of path:
            if (child) return child.findNodeByPath(path, startIndex, includeArchives);
            else return this;
        }).catch(() => this);
    }

    isLocal() {
        return this.local;
    }

    isFile() {
        return this.type == 'file' || this.type == 'virtualRemoteFile';
    }

    isLocalFolder() {
        return this.type == 'folder';
    }

    isFolder() {
        return this.isLocalFolder() || this.type === 'virtualRemoteFolder';
    }

    isRepo() {
        return this.type === 'repository' || this.type === 'virtualRemoteRepository';
    }

    isArchive() {
        return this.type === 'archive';
    }

    isInsideArchive() {
        return this.archivePath;
    }

    // If tabs or actions don't exist already - fetch them from the server
    load() {
        if (this.tabs || this.actions) {
            return this.$q.when(this);
        }
        else {
            let data = {
                type: this.type,
                path: this.path,
                repoKey: this.repoKey,
                text: this.text
            };
            return this.artifactoryHttpClient.post(this.RESOURCE.TREE_BROWSER, data)
                    .then((response) => {
                        this.tabs = response.data[0].tabs;
                        this.actions = response.data[0].actions;
                        return this;
                    });
        }
    }

    getDownloadPath() {
        if (!this.downloadPathPromise) {
            let data = {
                repoKey: this.repoKey,
                path: this.downloadPath || this.path
            };
            this.downloadPathPromise = this.artifactActionsDao.perform({action: 'download'}, data).$promise
                .then((response) => {
                    return this.actualDownloadPath = response.data.path;
                });
        }
        return this.downloadPathPromise;
    }

    refreshWatchActions() {
         // Can't watch archive / remote files
        if (!this.isInsideArchive() && this.isLocal()) {
            return this.artifactWatchesDao.status({repoKey: this.repoKey, path: this.path})
                .$promise.then((action) => {
//                    console.log(action);
                    let previousAction = this._getWatchAction();
                    if (action && action.name) {
                        // Replace the previous action with the new one
                        if (previousAction) previousAction.name = action.name;
                        // Or add the new one if didn't exist before
                        else this.actions.push(action);
                    }
                    else {
                        // Remove the previous action if there is no new action
                        if (previousAction) _.remove(this.actions, previousAction);
                    }
                    return this;
                });
        }
        else {
            return this.$q.when(this);
        }
    }
    _getWatchAction() {
        return _.find(this.actions, (action) => {
            return _.contains(['Watch', 'Unwatch'], action.name);
        });
    }


    invalidateChildren() {
        this.children = null;
    }

    invalidateParent() {
        if (this.parent) {
            this.parent.invalidateChildren();
        }
        else {
            this.treeBrowserDao.invalidateRoots();
        }
    }

    // Get the children of this node and cache the result
    // or return the cached promise
    getChildren(force = false) {
        // Server errors if requesting children of file
        // (simple browser always requests getChildren of current node)
        if (this.isFile()) {
            return this.$q.when(null);
        }

        if (!this.children || (force && !this.isInsideArchive())) {
            // Load children from server, and cache them
            this.children = this.treeBrowserDao._loadChildren({type: "junction", repoType: this.repoType, repoKey: this.repoKey, path: this.path, text: this.text}, this);
        }
        return this.children;
    }
}