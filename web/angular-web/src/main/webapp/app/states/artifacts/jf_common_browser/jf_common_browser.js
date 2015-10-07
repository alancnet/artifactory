import KEYS       from '../../../constants/keys.constants';
import ACTIONS from '../../../constants/artifacts_actions.constants';

const JSTREE_ROW_HOVER_CLASS = 'jstree-hovered';
const REGEXP = /(pkg|repo)\:(.+)/g;

export default class JFCommonBrowser {
    constructor(ArtifactActions) {
        this.artifactActions = ArtifactActions;
        this.activeFilter = false;

        if (this.browserController) {
            this.activeFilter = this.browserController.activeFilter || false;
            this.searchText = this.browserController.searchText || '';
            if (this.searchText.endsWith('*')) this.searchText = this.searchText.substr(0,this.searchText.length-1);
        }

    }

    /********************************************
     * Is the node matching the search criteria
     ********************************************/
    _searchCallback(str, jsTreeNode) {

        if (!jsTreeNode.data) return false;
        let treeNode = jsTreeNode.data;

        // Special filters:
        let filterRegexp = new RegExp(REGEXP);
        let matches = filterRegexp.exec(str);
        if (matches) {
            let filterType = matches[1];
            let filterText = matches[2];
            let rootRepo = this._getRootRepo(jsTreeNode).data;

            switch(filterType) {
                case 'pkg':
                    return (treeNode.isRepo() && treeNode.repoPkgType.toLowerCase().indexOf(filterText.toLowerCase()) != -1) || (!treeNode.isRepo() && this.activeFilter && (rootRepo.isRepo() && rootRepo.repoPkgType.toLowerCase().indexOf(filterText.toLowerCase()) != -1));
                case 'repo':
                    return (treeNode.isRepo() && treeNode.repoType.toLowerCase().indexOf(filterText.toLowerCase()) != -1) || (!treeNode.isRepo() && this.activeFilter && (rootRepo.isRepo() && rootRepo.repoType.toLowerCase().indexOf(filterText.toLowerCase()) != -1));
            }
        }
        // Regular text search:
        else {
            if (!this._isVisible(jsTreeNode)) return false;
            return treeNode.text && treeNode.text.indexOf(str) != -1;
        }
    }


    /****************************
     * Context menu items
     ****************************/

    _getContextMenuItems(obj, cb) {
        let actionItems = {};
        if (obj.data) {
            let node = obj.data;
            node.load()
            .then(() => node.refreshWatchActions())
            .then(() => node.getDownloadPath())
            .then(() => {
                if (node.actions) {
                    node.actions.forEach((actionObj) => {
                        let name = actionObj.name;
                        let action = angular.copy(ACTIONS[name]);
                        if (!action) {
                            console.log("Unrecognized action", name);
                            return true;
                        }
                        action.icon = 'action-icon icon ' + action.icon;
                        action.label = action.title;
                        if (actionObj.name === 'Download') {
                            action.link = node.actualDownloadPath;
                        }
                        else {                        
                            action.action = () => {
                                this.artifactActions.perform(actionObj, obj);
                            }
                        }
                        actionItems[name] = action;
                    });

                    cb(actionItems);
                }
                else {
                    cb([]);
                }
            });
        }
        else {
            cb([]);
        }
    }

    /****************************
     * Access methods
     ****************************/
    jstree() {
        return $(this.treeElement).jstree();
    }

    /****************************
     * Searching the tree
     ****************************/
    _searchTree(text) {
        if (!text) return;
        this.searchText = text || '';
        $(this.treeElement).unhighlight();
        let showOnlyMatches = text.match(new RegExp(REGEXP));
        this.jstree().search(this.searchText, false, showOnlyMatches);
    }

    _getSelectedTreeNode() {
        let selectedJsNode = this.jstree().get_node(this._getSelectedNode());
        return selectedJsNode && selectedJsNode.data;
    }

    _getSelectedNode() {
        return this.jstree().get_selected()[0];
    }


    _onSearch(e, data) {
        if (data.length == 0) {
            return;
        }
        this.searchResults = data.res;
        $(this.treeElement).highlight(this.searchText);
        if (!this.currentResult || !_.include(this.searchResults, this.currentResult)) {
            // there is no previous result, or previous result is not included in the search results
            // select first result that's below the node we started the search from
            let startFromDom = this.jstree().get_node(this._getSelectedNode(), /* as_dom = */ true)[0];
            let firstNodeBelow = _.find(data.nodes, (node) => {
                if (!startFromDom) return true;
                return node.offsetTop > startFromDom.offsetTop;
            });
            // if found - select as first result, if not - select first search result
            this.currentResult = firstNodeBelow ? firstNodeBelow.id : this.searchResults[0];
        }

        this._gotoCurrentSearchResult();
    }

    _isInActiveFilterMode() {
        if (this.searchText.match(new RegExp(REGEXP))) {
            let json = this.jstree().get_json();
            let matchesFound = false;
            for (let node of json) {
                node.data.isRepo = () => {
                    return node.data.type === 'repository' ||
                           node.data.type === 'virtualRemoteRepository' ||
                           node.data.type === 'localRepository' ||
                           node.data.type === 'remoteRepository' ||
                           node.data.type === 'cachedRepository' ||
                           node.data.type === 'virtualRepository';
                };
                if (this._searchCallback(this.searchText,node)) {
                    matchesFound = true;
                    break;
                }
            }
            return matchesFound ? true : 'no results';
        }
        else return false;
    }

    _searchTreeKeyDown(key) {
        let jstree = this.jstree();
        if (key == KEYS.DOWN_ARROW) {
            this._selectNextSearchResult();
        }
        else if (key == KEYS.UP_ARROW) {
            this._selectPreviousSearchResult();
        }
        else if (key == KEYS.ENTER) {
            //manually set the model to the input element's value (because the model is debounced...)
            this.searchText = $('.jf-tree-search').val();

            if (this._isInActiveFilterMode() === true) {
                this.activeFilter = true;
                if (this.browserController) {
                    this.browserController.activeFilter = true;
                    this.browserController.searchText = this.searchText + '*';
                }
                this._searchTree(this.searchText);
                this._focusOnTree();
                if (!this._isVisible(jstree.get_node(this._getSelectedNode()))) {
                    jstree.select_node(this._getFirstVisibleNode());
                }
            }
            else if (this._isInActiveFilterMode() === 'no results') {
                if (this.artifactoryNotifications) this.artifactoryNotifications.create({warn: "No repositories matches the filtered " + (this.searchText.startsWith('pkg:') ? 'package' : 'repository') + " type"});
            }
            else {
                this.activeFilter = false;
                if (this.browserController) this.browserController.activeFilter = false;
                this._selectCurrentSearchResult();
                jstree.open_node(this.currentResult);
                this._clear_search();
                this._focusOnTree();
                this.currentResult = null;
            }
        }
        else if (key == KEYS.ESC) {
            this.activeFilter = false;
            if (this.browserController) this.browserController.activeFilter = false;
            this._clear_search();
            this._focusOnTree();
            this.currentResult = null;
        }
    }

    _clear_search() {
        this.activeFilter = false;
        if (this.browserController) this.browserController.activeFilter = false;
        this._unhoverAll();
        this.jstree().clear_search();
        $(this.treeElement).unhighlight();
    }

    _selectNextSearchResult() {
        let index = this.searchResults.indexOf(this.currentResult);
        index++;
        if (index > this.searchResults.length - 1) {
            index = 0;
        }
        this.currentResult = this.searchResults[index];
        this._gotoCurrentSearchResult();
    }

    _selectPreviousSearchResult() {
        let index = this.searchResults.indexOf(this.currentResult);
        index--;
        if (index < 0) {
            index = this.searchResults.length - 1;
        }
        this.currentResult = this.searchResults[index];
        this._gotoCurrentSearchResult();
    }

    _gotoCurrentSearchResult() {
        this._unhoverAll();
        if (this.currentResult) {
            let domElement = this._getDomElement(this.currentResult);
            this._hover(domElement);
            this._scrollIntoView(domElement);
        }
    }

    _selectCurrentSearchResult() {
        if (this.currentResult) {
            this.jstree().deselect_all();
            this.jstree().select_node(this.currentResult);
        }
    }

    /****************************
     * access the tree
     ****************************/

    _isVisible(jsTreeNode) {
        // If the node is hidden, the get_node as DOM returns empty result
        return this.jstree().get_node(jsTreeNode, true).length && $('#'+this._getSafeId(jsTreeNode.id)).css('display') !== 'none';
    } 

    _isRootRepoVisible(jsTreeNode) {
        return this.jstree().get_node(this._getRootRepo(jsTreeNode), true).length;
    }

    _getFirstVisibleNode() {
        let json = this.jstree().get_json();
        for (let node of json) {
            if (this._isVisible(node)) {
                return node;
            }
        }
    }

    _getRootRepo(jsTreeNode) {
        if (!jsTreeNode.parents || jsTreeNode.parents.length === 1) return jsTreeNode;
        let rootRepoId = jsTreeNode.parents[jsTreeNode.parents.length-2];
        return this.jstree().get_node(rootRepoId);
    }

    _unhoverAll() {
        $('.' + JSTREE_ROW_HOVER_CLASS).removeClass(JSTREE_ROW_HOVER_CLASS);
    }

    _hover(domElement) {
        domElement.find('.jstree-anchor').first().addClass(JSTREE_ROW_HOVER_CLASS);
    }

    _focusOnTree() {
        // Make sure we can continue navigating the tree with the keys
        this._getSelectedJQueryElement().focus();
    }

    _getSelectedJQueryElement() {
        let nodeID = this._getSafeId(this.jstree().get_selected()[0]);
        return $('.jstree #' + nodeID + '_anchor');
    }

    _getSafeId(id) {
        return this._escapeChars(id,['/','.','$','{','}','(',')','[',']']);
    }

    _escapeChars(str,chars) {
        let newStr = str;
        chars.forEach((char)=>{
            newStr = newStr ? newStr.split(char).join('\\'+char) : newStr;
        });
        return newStr;
    }

    _getDomElement(node) {
        return this.jstree().get_node(node, true);
    }

    _scrollIntoView(domElement) {
$
        if (!domElement || !domElement[0]) return;

        if (domElement[0].scrollIntoViewIfNeeded) {
            domElement[0].scrollIntoViewIfNeeded(true);
        }
        else {
            this._scrollToViewIfNeededReplacement(domElement[0],true);
        }
    }

    _scrollToViewIfNeededReplacement(elem,centerIfNeeded) {
        centerIfNeeded = arguments.length <= 1 ? true : !!centerIfNeeded;

        var parent = elem ? elem.offsetParent : null;

        if (!parent) return;

        var     parentComputedStyle = window.getComputedStyle(parent, null),
                parentBorderTopWidth = parseInt(parentComputedStyle.getPropertyValue('border-top-width')),
                parentBorderLeftWidth = parseInt(parentComputedStyle.getPropertyValue('border-left-width')),
                overTop = elem.offsetTop - parent.offsetTop < parent.scrollTop,
                overBottom = (elem.offsetTop - parent.offsetTop + elem.clientHeight - parentBorderTopWidth) > (parent.scrollTop + parent.clientHeight),
                overLeft = elem.offsetLeft - parent.offsetLeft < parent.scrollLeft,
                overRight = (elem.offsetLeft - parent.offsetLeft + elem.clientWidth - parentBorderLeftWidth) > (parent.scrollLeft + parent.clientWidth),
                alignWithTop = overTop && !overBottom;

        if ((overTop || overBottom) && centerIfNeeded) {
            parent.scrollTop = elem.offsetTop - parent.offsetTop - parent.clientHeight / 2 - parentBorderTopWidth + elem.clientHeight / 2;
        }

        if ((overLeft || overRight) && centerIfNeeded) {
            parent.scrollLeft = elem.offsetLeft - parent.offsetLeft - parent.clientWidth / 2 - parentBorderLeftWidth + elem.clientWidth / 2;
        }

        if ((overTop || overBottom || overLeft || overRight) && !centerIfNeeded) {
            elem.scrollIntoView(alignWithTop);
        }
    }

}