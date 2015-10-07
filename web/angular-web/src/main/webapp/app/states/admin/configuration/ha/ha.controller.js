let HaDao, $scope, artifactoryGridFactory, modal;
const installLink   = 'http://www.jfrog.com/confluence/display/RTF/Installation+and+Setup';
const wikiLink = 'http://www.jfrog.com/confluence/pages/viewpage.action?pageId=25067914';

export class AdminConfigurationHAController {

    constructor(_$scope_, _ArtifactoryModal_, _HaDao_, _ArtifactoryGridFactory_, _ArtifactoryState_) {
        HaDao = _HaDao_;
        $scope = _$scope_;
        artifactoryGridFactory = _ArtifactoryGridFactory_;
        modal = _ArtifactoryModal_;

        this.installLink = installLink;
        this.wikiLink = wikiLink;
        this.gridOptions = {};
        this._createGrid();
        this._initHa();
    }

    _initHa() {
        HaDao.query().$promise.then((ha)=> {
            this.ha = ha;
            this.gridOptions.setGridData(this.ha)
        });
    }

    _createGrid() {
        this.gridOptions = artifactoryGridFactory.getGridInstance($scope)
                .setColumns(this._getColumns())
                .setButtons(this._getActions())
                .setRowTemplate('default');
    }


    _getColumns() {
        return [
            {
                field: "id",
                name: "ID",
                displayName: "ID",
                width: '7%'},
            {
                field: "startTime",
                name: "Start Time",
                displayName: "Start Time",
                width: '11%'
            },
            {
                field: "url",
                name: "URL",
                displayName: "URL",
                width: '19%',
                cellTemplate: '<div class="ui-grid-cell-contents"><a target="_blank" href="{{ COL_FIELD }}">{{ COL_FIELD }}</a></div>'
            },
            {
                field: "memberShipPort",
                name: "Membership Port",
                displayName: "Membership Port",
                width: '11%'
            },
            {
                field: "state",
                name: "State",
                displayName: "State",
                width: '7%'
            },
            {
                field: "role",
                name: "Role",
                displayName: "Role",
                width: '7%'
            },
            {
                field: "lastHeartbeat",
                name: "Last Heartbeat",
                displayName: "Last Heartbeat",
                width: '11%',
                cellTemplate: `
                    <div ng-if="row.entity.heartbeatStale"
                         class="ui-grid-cell-contents ha-heartbeat-stale"
                         jf-tooltip="Heartbeat is stale. Check if your server is down."><i class="icon icon-notif-warning"></i>{{ COL_FIELD }}</div>
                    <div ng-if="!row.entity.heartbeatStale"
                         class="ui-grid-cell-contents">{{ COL_FIELD }}</div>
                         `
            },
            {
                field: "version",
                name: "Version",
                displayName: "Version",
                width: '9%'
            },
            {
                field: "revision",
                name: "Revision",
                displayName: "Revision",
                width: '7%'
            },
            {
                field: "releaseDate",
                name: "Release Date",
                displayName: "Release Date",
                width: '11%'
            }
        ]
    }

    _deleteNode(node) {
        modal.confirm('Are you sure you wish to remove ' + node.id + ' from the nodes list?')
            .then(() => HaDao.delete({id: node.id}))
            .then(() => this._initHa());
    }

    _getActions() {
        return [
            {
                icon: 'icon icon-clear',
                tooltip: 'Delete',
                visibleWhen: node => node.heartbeatStale,
                callback: node => this._deleteNode(node)
            }
        ];
    }
}