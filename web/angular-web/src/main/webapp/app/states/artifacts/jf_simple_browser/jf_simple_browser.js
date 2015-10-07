import TreeConfig from './jf_simple_tree.config';
import EVENTS     from '../../../constants/artifacts_events.constants';
import JFCommonBrowser from '../jf_common_browser/jf_common_browser';

class JfSimpleBrowserController extends JFCommonBrowser {
    constructor($element, $stateParams, $scope, $timeout, $q, TreeBrowserDao, ArtifactoryEventBus, NativeBrowser, ArtifactoryState, ArtifactActions) {
        super(ArtifactActions);
        this.$stateParams = $stateParams;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.$element = $element;
        this.$q = $q;
        this.currentNode = {};
        this.treeBrowserDao = TreeBrowserDao;
        this.nativeBrowser = NativeBrowser;
        this.artifactoryState = ArtifactoryState;
        this.treeElement = $(this.$element).find('#tree-element');
        this._registerEvents();
    }

    // This is called from link function
    initJSTree() {
        if (_.isEmpty(this.$stateParams.artifact)) {
            // load empty parent (roots)
            this._loadNodeIntoView();
        }
        else {
            // load artifact by path
            this._loadNodeByPath(this.$stateParams.artifact);
        }
    }

     // Preload data for the selected artifact and load it into view
    _loadNodeByPath(path) {
        if (path) {
            this.treeBrowserDao.findNodeByFullPath(path, /* includeArchives = */false)
                .then((node) => this._loadNodeIntoView(node));
        }
        else {
            this._loadNodeIntoView();
        }
    }

    /***************************************************
     * Load the node's data and children (if applicable)
     ***************************************************/
    _loadNodeIntoView(node) {
        if (node) this.selectedNode = node;
        let promise;
         // Not drilling down to repo if didn't click on it
        if (node && (node.parent || this.artifactoryState.getState('tree_touched'))) {
            if (!node.isFolder() && !node.isRepo()) // Not drilling down to files / archives
            {
                this.currentParentNode = node.parent;
            }
            else {
                this.currentParentNode = node;
            }
            promise = this._loadParentIntoView(this.currentParentNode);
        }
        else {
            this.currentParentNode = null;
            promise = this._loadRootsIntoView();
        }
        promise.then(() => this._dispatchEvent());
    }

    _loadParentIntoView(node) {
        // (Adam) Don't use ng-class, it causes major performance issue on large data sets
        this.treeElement.addClass('has-parent');
        return this._loadChildren(node.getChildren());
    }
    
    _loadRootsIntoView() {
        // (Adam) Don't use ng-class, it causes major performance issue on large data sets
        this.treeElement.removeClass('has-parent');   
        return this._loadChildren(this.treeBrowserDao.getRoots());
    }

    _loadChildren(promise) {
        return promise.then((children) => {
            // select first child if none selected
            this.selectedNode = this.selectedNode || children[0];
            children = this._transformData(children || []);
            if (this.currentParentNode) {
                // Create a tree with parent and children
                let goUp = {
                    type: 'go_up',
                    data: this.currentParentNode.parent,
                    text: '..'
                };
                let parentTreeNode = this._transformNode(this.currentParentNode);
                parentTreeNode.children = children;
                parentTreeNode.state.opened = true;

                this._buildTree([goUp, parentTreeNode]);
            }
            else {
                // Create a tree with only children
                this._buildTree(children);
            }
        });
    }

    _transformData(data) {
        return data.map((node) => this._transformNode(node));
    }

    _transformNode(node) {
        let nodeText;
        if (this.nativeBrowser.isAllowed(node)) {
            // TODO: remove -> once we have the icon in the SVG
            nodeText = `${ node.text }
                <a onclick="event.stopImmediatePropagation()"
                   class="view-in-simple-mode"
                   target="_blank"
                   title="Directory Browsing"
                   href="${ this.nativeBrowser.pathFor(node) }">
                    <i class="icon icon-simple-browser"></i>
                </a>`;
        }
        else {
            nodeText = `<span class="no-simple-browsing">${ node.text }</span>`;
        }
        return {
            text: nodeText,
            data: node,
            type: node.iconType,
            state: {
                selected: this.selectedNode === node
            }
        };
    }

    _toggleCompactFolders() {
        this.treeBrowserDao.invalidateRoots();
        this.initJSTree();
    }

    _registerTreeEvents() {
        $(this.treeElement).on("search.jstree", (e, data) => this._onSearch(e, data));

        $(this.treeElement).on("ready.jstree", (e) => this._onReady(e));
        $(this.treeElement).on("close_node.jstree", (e, args) => {
            this.jstree().open_node(args.node);
        });
        $(this.treeElement).on("select_node.jstree", (e, args) => {
            this.artifactoryState.setState('tree_touched', true);
            let treeNode = args.node.data;

            if (treeNode !== this.currentParentNode && // Not selecting the current parent
                (!treeNode || // going up to roots
                treeNode.isFolder() || treeNode.isRepo())) // drilling down to folder or repo
            {
                this._loadNodeIntoView(treeNode);
            }
            else {
                // just select (no need to refresh current tree)
                this.selectedNode = treeNode;
                this._dispatchEvent();
            }
        });


    }
    
    _dispatchEvent() {
        if (!this.selectedNode) return;
        // Make sure tree data is loaded
        this.selectedNode.load().then(() => {
            // Then dispatch TREE_NODE_SELECT event
            this.artifactoryEventBus.dispatch(EVENTS.TREE_NODE_SELECT, {data: this.selectedNode});
        });
    }

    _onReady() {
        if (!this.selectedNode.parent && this.activeFilter) this._searchTree(this.searchText);

        this.jstree().show_dots();
        this._focusOnTree();
    }


    /****************************
     * Event registration
     ****************************/
    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_CHANGE, text => this._searchTree(text));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_CANCEL, text => this._clear_search());
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_SEARCH_KEYDOWN, key => this._searchTreeKeyDown(key));

        // Must destroy jstree on scope destroy to prevent memory leak:
        this.$scope.$on('$destroy', () => {
            if (this.jstree()) {
                this.jstree().destroy();
            }
        });

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_DEPLOY, (repoKey) => {
            this.artifactoryState.setState('tree_touched', true); // Make sure we go inside the repo and not stay at the root level
            this.treeBrowserDao.findRepo(repoKey)
                .then((repoNode) => this._loadNodeIntoView(repoNode));
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_REFRESH, (node) => {
            if (node.data != this.currentParentNode) return;
            if (node.data) node.data.invalidateChildren();
            else this.treeBrowserDao.invalidateRoots();
            this._loadNodeIntoView(node.data);
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_DELETE, (node) => {
            this.$timeout(() => this._loadNodeIntoView(this.currentParentNode.parent), 500);
        });
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_MOVE, (options) => this._openTargetNode(options));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ACTION_COPY, (options) => this._openTargetNode(options));
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_COMPACT, () => this._toggleCompactFolders());

        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.ARTIFACT_URL_CHANGED, (stateParams) => {
            if (stateParams.browser != 'simple') return;
            // URL changed (like back button / forward button / someone input a URL)
            let currentNodePath = this.selectedNode && this.selectedNode.fullpath || '';
            if (currentNodePath != stateParams.artifact || stateParams.forceLoad) {
                this._loadNodeByPath(stateParams.artifact);
            }
        });

    }

    _openTargetNode(options) {
        this.$timeout(() => {
            let fullpath = _.compact([options.target.targetRepoKey, options.target.targetPath, options.node.data.text]).join('/');
            this.treeBrowserDao.invalidateRoots();
            this._loadNodeByPath(fullpath);
        }, 500);
    }


    /****************************
     * Build the JSTree from the nodes
     ****************************/
    _buildTree(data) {
        TreeConfig.core.data = data;

        TreeConfig.contextmenu.items = this._getContextMenuItems.bind(this);

        // Search by node text only (otherwise searches the whole HTML)
        TreeConfig.search.search_callback = this._searchCallback.bind(this);

        if (this.built) this.jstree().destroy();
        $(this.treeElement).jstree(TreeConfig);
        this.built = true;
        this._registerTreeEvents();
    }


    // setCurrentTab(tab) {
    //     this.currentTab = tab;
    // }

    // isCurrentTab(tab) {
    //     return this.currentTab === tab;
    // }

}

export function jfSimpleBrowser() {
    return {
        scope: {
            browserController: '='
        },
        restrict: 'E',
        controller: JfSimpleBrowserController,
        controllerAs: 'SimpleBrowser',
        bindToController: true,
        link: ($scope, attrs, $element, SimpleBrowser) => SimpleBrowser.initJSTree(),
        templateUrl: 'states/artifacts/jf_simple_browser/jf_simple_browser.html'
    }
}
