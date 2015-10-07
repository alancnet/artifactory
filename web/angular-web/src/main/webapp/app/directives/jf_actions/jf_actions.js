import EVENTS from '../../constants/artifacts_events.constants';
import ACTIONS from '../../constants/artifacts_actions.constants';

class jfActionsController {
    constructor($scope, ArtifactoryEventBus, $element, ArtifactActionsDao, ArtifactActions) {
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.$scope = $scope;
        this.artifactActions = ArtifactActions;
        this.artifactActionsDao = ArtifactActionsDao;
        this.isDropdownOpen = false;

        this._registerEvents();


    }

    selectNode(node) {
        if (node.data) {
            this.selectedNode = node;
            this.selectedNode.data.getDownloadPath()
                .then(() => this._setActions(node.data.actions));
        }
        else {
            this.selectedNode = null;            
            this._setActions([]);
        }
    }

    _setActions(actions) {
        this.actions = actions || [];
        this._transformActionsData();
    }

    doAction(actionObj) {
        if (!actionObj.href) this.artifactActions.perform(actionObj, this.selectedNode);
    }

    _toggleDropdown(isOpen) {
        this.isDropdownOpen = isOpen;
        if (!this.$scope.$$phase) {
            this.$scope.$digest();
        }
    }

    _registerEvents() {
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TREE_NODE_SELECT, node => this.selectNode(node));
        this.artifactoryEventBus.registerOnScope(this.$scope, [EVENTS.ACTION_WATCH, EVENTS.ACTION_UNWATCH], () => {
                this.selectedNode.data.refreshWatchActions()
                .then(() => {
                    this._setActions(this.selectedNode.data.actions);
                });
            });
    }

    // Transform from server JSON to client representation
    _transformActionsData() {
        // extend action properties from ACTIONS dictionary
        this.actions.forEach((actionObj) => {
            if (!ACTIONS[actionObj.name]) {
                console.log("Unrecognized action", actionObj.name);
                return true;
            }
            angular.extend(actionObj, ACTIONS[actionObj.name]);
            if (actionObj.name === 'Download') {                
                actionObj.href = this.selectedNode.data.actualDownloadPath;
            }
        });
        // Divide actions to fixed and dynamic (dropdown)
        this._divideActions();
    }

    _divideActions() {
        this.fixedActions = [];
        this.dynamicActions = [];
        _.forEach(this.actions, (actionObj) => {
            if (actionObj.name === 'Download' || actionObj.name === 'View') {
                this.fixedActions.push(actionObj);
            }
            else {
                this.dynamicActions.push(actionObj);
            }
        });
        if (this.fixedActions.length === 0 && this.dynamicActions.length === 1) {
            this.fixedActions.push(this.dynamicActions.pop());
        }
    }
}

export function jfActions($timeout) {
    return {
        restrict: 'EA',
        controller: jfActionsController,
        controllerAs: 'jfActions',
        templateUrl: 'directives/jf_actions/jf_actions.html',
        bindToController: true,
        link: function ($scope, element, attr, jfActions) {
            let dropdownOver = false;
            let buttonOver = false;
            let $dropdownElement = $(element).find('.actions-more');
            let $buttonElement = $(element).find('.action-button');
            let stayOpened = false;
            let clicked = false;

            $dropdownElement.on('mouseup', () => {
                if (!stayOpened) {
                    clicked = true;
                }
            });

            $dropdownElement.on('mouseenter', () => {
                buttonOver = true;
                jfActions._toggleDropdown(true);
            });
            $dropdownElement.on('mouseleave', () => {
                buttonOver = false;
                if (!stayOpened) {
                    jfActions._toggleDropdown(dropdownOver);
                }
            });
            $buttonElement.on('mouseenter', () => {
                dropdownOver = true;
                jfActions._toggleDropdown(true);
            });
            $buttonElement.on('mouseleave', () => {
                dropdownOver = false;
                if (!stayOpened) {
                    $timeout(()=> {
                        jfActions._toggleDropdown(buttonOver || dropdownOver);
                    }, 200);
                }
            });


            let unwatch = $scope.$watch('jfActions.isDropdownOpen',(newVal,oldVal)=>{
                if (!newVal && clicked) {
                    jfActions.isDropdownOpen = true;
                    clicked = false;
                    stayOpened = true;
                }
                else if (!newVal && stayOpened) {
                    stayOpened = false;
                }

            });

            $scope.$on('$destroy', () => {
                $dropdownElement.off('click');
                unwatch();
            });

        }
    };
}