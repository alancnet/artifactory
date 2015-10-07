import EVENTS from '../../../../constants/artifacts_events.constants';
import DICTIONARY from './../../constants/artifact_general.constant';
import TOOLTIP from '../../../../constants/artifact_tooltip.constant';
const headerCellGroupingTemplate = require("raw!../../../../ui_components/artifactory_grid/templates/headerCellTemplate.html");
const infoPropertyBlacklist = ['kbComponentId', 'kbReleaseId', 'catalogComponent', 'compponentLink'];

class jfGovernanceController {

    constructor($scope, GovernanceDao, ArtifactoryEventBus, ArtifactoryGridFactory, ArtifactoryNotifications) {
        this.$scope = $scope;
        this.GovernanceDao = GovernanceDao;
        this.artifactoryGridFactory = ArtifactoryGridFactory;
        this.artifactoryNotifications = ArtifactoryNotifications;
        this.artifactoryEventBus = ArtifactoryEventBus;
        this.DICTIONARY = DICTIONARY.governance;
        this.governanceGridOptions = null;
        this.TOOLTIP = TOOLTIP.builds;

        this._init();
    }

    _init() {
        this._createGrid();
        this._getData();
        this._registerEvents();
    }

    _getData() {
        this.GovernanceDao.fetch({
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path
        }).$promise
            .then((data) => {
                if (data.warn) {
                    this.governanceData = {feedbackMsg: data};
                } else {
                    this.governanceData = data;
                    this.governanceGridOptions.setGridData(data.vulnerabilities || []);
                }

            }).catch((err)=>{
                    //console.log(err);
                    if (err.data && err.data.feedbackMsg && err.data.feedbackMsg.error) {
                        this.artifactoryNotifications.create({error: err.data.feedbackMsg.error});
                        this.governanceData = err.data;
                    }
                });
    }

    _createGrid() {
        this.governanceGridOptions = this.artifactoryGridFactory.getGridInstance(this.$scope)
            .setRowTemplate('default')
            .setColumns(this._getColumns());
    }

    _getColumns() {
        return [
            {
                name: "Artifact ID",
                displayName: 'Artifact ID',
                field: "artifactID",
                headerCellTemplate: headerCellGroupingTemplate
            },
            {
                name: "Name",
                displayName: "Name",
                field: "name",
                headerCellTemplate: headerCellGroupingTemplate
            },
            {
                name: "Severity",
                displayName: "Severity",
                field: "severity",
                headerCellTemplate: headerCellGroupingTemplate
            },
            {
                name: "Description",
                displayName: "Description",
                field: "description",
                headerCellTemplate: headerCellGroupingTemplate
            }
        ]
    }

    _registerEvents() {
        let self = this;
        this.artifactoryEventBus.registerOnScope(this.$scope, EVENTS.TAB_NODE_CHANGED, (node) => {
            if (this.currentNode != node) {
                this.currentNode = node;
                self._getData();
            }
        });
    }

    propertyShouldDisplay(property) {
        return !_.contains(infoPropertyBlacklist, property);
    }

    updateComponentId(componentId) {
        this.GovernanceDao.update({
            "repoKey": this.currentNode.data.repoKey,
            "path": this.currentNode.data.path,
            "componentId": componentId,
            "origComponentId": this.governanceData.info.componentId
        }).$promise.then(() => this._getData());
        this.editMode = false;
    }

    cancelEditMode($event) {
        let element = $($event.toElement);
        if (!element.hasClass('fa') && element.prop('tagName') !== 'INPUT') {
            this.editMode = false;
        }
    }

}
export function jfGovernance() {
    return {
        restrict: 'EA',
        controller: jfGovernanceController,
        controllerAs: 'jfGovernance',
        bindToController: true,
        templateUrl: 'states/artifacts/jf_artifact_info/info_tabs/jf_governance.html',
        scope: {
            currentNode: '='
        }
    }
}