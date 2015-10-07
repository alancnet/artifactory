import TreeConfig from './jf_tree_browser.config';
import EVENTS     from '../../../constants/artifacts_events.constants';
import JFCommonBrowser from '../jf_common_browser/jf_common_browser';
/**
 * wrapper around the jstree jquery component
 * @url http://www.jstree.com/
 *
 * @returns {{restrict: string, controller, controllerAs: string, bindToController: boolean}}
 */
export function jfTreeBrowser() {
    return {
        scope: {
            browserController: '='
        },
        restrict: 'E',
        controller: JFTreeBrowserController,
        controllerAs: 'jfTreeBrowser',
        templateUrl: 'states/artifacts/jf_tree_browser/jf_tree_browser.html',
        bindToController: true,
        link: function ($scope) {
            $scope.jfTreeBrowser.initJSTree();
        }
    }
}

const ARCHIVE_MARKER = '!';

class JFTreeBrowserController extends JFCommonBrowser {
    constructor($timeout, ArtifactoryEventBus, $element, $scope, TreeBrowserDao, $stateParams, $q, ArtifactoryState, ArtifactActions, ArtifactoryNotifications) {
        super(ArtifactActions);
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$stateParams = $stateParams;
        this.$q = $q;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.treeBrowserDao = TreeBrowserDao;
        this.artifactoryState = ArtifactoryState;
        if (_.isEmpty($stateParams.artifact)) {
            // Important to know for switching to simple browser
            this.whenTreeDataLoaded = $q.when([]);
        }
        else {
            this.whenTreeDataLoaded = TreeBrowserDao.findNodeByFullPath($stateParams.artifact); // Preload data for the current selected artifact
        }

        this.$element = $element;
    }


    /****************************
     * Init code
     ****************************/

    // This is called from link function
    initJSTree() {
        // preload artifact
        this.whenTreeDataLoaded.then(() => {
            this.treeElement = $(this.$element).find('#tree-element');
            this._registerEvents();
            this._buildTree();
            this._registerTreeEvents();
        });
    }

    /**
     * When JStree is ready load the current browsing path from URL
     * and restore the nodes open and selected state.
     * @param e
     * @private
     */
    _openTreeNode(artifact) {
        let deferred = this.$q.defer();
        let jstree = this.jstree();
        let root = jstree.get_node('#');
        let path = _.trim(artifact?artifact.replace('//', '/'):'', '/').split('/');

        this._openNodePath(root, path, jstree.get_node(root.children[0]), (selectedNode) => {
            jstree.deselect_all();
            // Select the node
            jstree.select_node(selectedNode);

            // scroll the node into view
            let domElement = this._getDomElement(selectedNode);
            this._scrollIntoView(domElement);
            this._focusOnTree();
            deferred.resolve();
        });
        return deferred.promise
    }

    _onReady() {

        this._openTreeNode(this.$stateParams.artifact);
        this.jstree().show_dots();
    }

    /****************************
     * Event registration
     ****************************/
    _registerEvents() {
        // Must destroy jstree on scope destroy to prevent memory leak:
        this.$scope.$on('$destroy', () => {
            if (this.jstree()) {
                this.jstree().destroy();
            }
        });

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_CHANGE, text => this._searchTree(text));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_CANCEL, text => this._clear_search());
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_KEYDOWN, key => this._searchTreeKeyDown(key));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_DEPLOY, repoKey => this._refreshRepo(repoKey));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_REFRESH, node => this._refreshFolder(node));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_REFRESH, (node) => node ? this._refreshFolder(node) : this._refreshTree());
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_DELETE, (node) => {
            this._refreshParentFolder(node); // Refresh folder of node's parent
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_MOVE, (options) => {
            this._refreshParentFolder(options.node); // Refresh folder of node's parent
            this._refreshFolderPath(options); // Refresh target folder where node was copied
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_COPY, (options) => {
            this._refreshFolderPath(options);
        });

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_NODE_OPEN, path => {
            this._openTreeNode(path)
        });
        
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_COMPACT, () => this._toggleCompactFolders());

        // URL changed (like back button / forward button / someone input a URL)
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ARTIFACT_URL_CHANGED, (stateParams) => {
            if (stateParams.browser != 'tree') return;
            // Check if it's already the current selected node (to prevent opening the same tree node twice)
            let selectedNode = this._getSelectedTreeNode();
            if (selectedNode && selectedNode.fullpath === stateParams.artifact) return;
            this.treeBrowserDao.findNodeByFullPath(stateParams.artifact)
                .then(() => this._openTreeNode(stateParams.artifact));
        });
    }

    /**
     * register a listener on the tree and delegate to
     * relevant methods
     *
     * @param element
     * @private
     */
    _registerTreeEvents() {
        $(this.treeElement).on("search.jstree", (e, data) => this._onSearch(e, data));
        $(this.treeElement).on("ready.jstree", (e) => this._onReady(e));
        $(this.treeElement).on("select_node.jstree", (e, args) => {
            if (args.event) { // User clicked / pressed enter
                this.artifactoryState.setState('tree_touched', true);
            }
            this._loadNode(args.node);
        });
        $(this.treeElement).on("activate_node.jstree", (e, args) => {
            if (args.event) { // User clicked / pressed enter
                this.artifactoryState.setState('tree_touched', true);
            }

            if (!args.node.data.isArchive() && args.node.data.icon !== 'docker') this.jstree().open_node(args.node);
        });

        $(this.treeElement).on("after_open.jstree",(node)=>{
            if (this.activeFilter) this._searchTree(this.searchText);
        });
    }

    _loadNode(item) {
        item.data.load().then(() => this.artifactoryEventBus.dispatch(EVENTS.TREE_NODE_SELECT, item));
    }

    /****************************
     * Compact folders
     ****************************/
    _toggleCompactFolders() {
        this._refreshTree();
    }

    /****************************
     * Building the tree
     ****************************/
    _buildTree() {
        let asyncStateLoad = (obj, cb) => {
            let promise;
            if (obj.id === '#') {
                promise = this.treeBrowserDao.getRoots();
            }
            else {
                promise = obj.data.getChildren();
            }
            promise.then((data) => {
                cb(this._transformData(data));
            });
        };

        TreeConfig.core.data = asyncStateLoad;
        TreeConfig.contextmenu.items = this._getContextMenuItems.bind(this);

                // Search by node text only (otherwise searches the whole HTML)
        TreeConfig.search.search_callback = this._searchCallback.bind(this);

        $(this.treeElement).jstree(TreeConfig);
    }

    _transformData(data) {
        data = data || [];
        return data.map((node) => {
            let item = {};
            item.children = node.hasChild;
            item.text = node.text;
            item.data = node;
            item.type=node.iconType;
            return item;
        });
    }

    /****************************
     * Refreshing the tree
     ****************************/

    /**
     * refresh children of folder
     *
     * @param node
     * @private
     */
    _refreshRepo(repoKey) {
        let jstree = this.jstree();
        let root = jstree.get_node('#');
        let repoJsNode;
        _.each(root.children, (child) => {
            repoJsNode = jstree.get_node(child);
            if (repoJsNode && repoJsNode.data && repoJsNode.data.repoKey === repoKey) return false;
        });
        //console.log(repoJsNode.data.repoKey);
        if (repoJsNode) {
            repoJsNode.data.invalidateChildren();
            jstree.load_node(repoJsNode, () => {
                jstree.select_node(repoJsNode);
            });
        }
    }

    _refreshFolder(node) {
        if (node.data) node.data.invalidateChildren();
        else this.treeBrowserDao.invalidateRoots();
        this.jstree().load_node(node);
    }

    _refreshParentFolder(node) {
        node.data.invalidateParent();
        let parentNodeItem = this.jstree().get_node(node.parent);
        this.$timeout(() => {        
            this._refreshFolder(parentNodeItem);
            this.jstree().select_node(parentNodeItem);
        }, 500);
    }

    _refreshFolderPath(option) {
        let targetPath = _.compact(option.target.targetPath.split('/'));
        let path = [option.target.targetRepoKey].concat(targetPath);

        let curNode = this.jstree().get_node('#');

        let childNode = this._getChildByPath(curNode, path);
        if (childNode && _.isArray(childNode.children)) {
            curNode = childNode;
        }

        // Data is still not refreshed on server
        this.$timeout(()=> {
            if (curNode && curNode.data) {
                this._refreshFolder(curNode);
                curNode.data.getChildren().then(()=> {
                    this._openTreeNode(option.target.targetRepoKey + '/' + option.target.targetPath + '/' + option.node.data.text)
                });
            }
            else {
                this._openTreeNode(option.target.targetRepoKey + '/' + option.target.targetPath + '/' + option.node.data.text);
            }
        }, 500);
    }

    _refreshTree() {
        this.treeBrowserDao.invalidateRoots();
        this.jstree().refresh();
    }

    /****************************
     * Traversing the tree
     ****************************/

     /**
     * Find the next child by path. Take into account the node's text by consist of some of the path elements (in compact mode)
     * @param parentNode:Object node object from where to start
     * @param path:Array array of path elements
     * @returns childNode or undefined
     * @private
     */    
    _getChildByPath(parentNode, path) {
        let jstree = this.jstree();
        let children = this._getChildrenOf(parentNode);
        // Find the node that conforms to the largest subpath of path 
        for(let i = path.length; i > 0; i--) {
            let subpath = path.slice(0, i);
            let testPathStr = _.trimRight(subpath.join('/'), ARCHIVE_MARKER);
            let result = _.find(children, (childNode) => {
                // Sometimes the node's text is not the full text (like for docker images)
                let childPath = childNode.data.fullpath;

                if (childPath === testPathStr || childPath === testPathStr + '/') {
                    return childNode;
                }
            });
            if (result) return result;
        }
    }

    _getChildrenOf(parentNode) {
        let jstree = this.jstree();
        return _.compact(parentNode.children.map((jsTreeNodeId) => jstree.get_node(jsTreeNodeId)));
    }

    /**
     * Open the path starting from the root node, and call the callback with the leaf node
     * and restore the nodes open and selected state.
     * @param node:Object node object from where to start
     * @param path:Array array of path elements
     * @param selectedNode:Object default node to return if not found
     * @param callback:Function callback to call with leaf node once the traversing is complete
     * @private
     */
    _openNodePath(node, path, leafNode, callback, pathStopIndex = 1) {
        let jstree = this.jstree();
        let childNode;
        while(pathStopIndex <= path.length) {
            let testPath = path.slice(0, pathStopIndex);
            childNode = this._getChildByPath(node, testPath);
            if (childNode) break;
            pathStopIndex++;
        }

        if (childNode) {
            leafNode = childNode;
            if (path.length === 0) {
                callback(leafNode);
            }
            else {
                jstree.open_node(leafNode, (node) => {
                    this._openNodePath(leafNode, path, leafNode, callback, pathStopIndex + 1);
                }, false);
            }
        }
        else {
            callback(leafNode);
        }
    }

}