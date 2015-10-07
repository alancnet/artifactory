class jfBuildInfoJsonController {
    constructor($stateParams, BuildsDao) {
        this.$stateParams = $stateParams;
        this.json = '';
        this.buildsDao = BuildsDao;


        this._getJson();

    }


    _getJson() {

        this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action: 'buildJson'
        }).$promise.then((data) => {
            this.json = data.fileContent;
        })
    }

}




export function jfBuildInfoJson() {
    return {
        restrict: 'EA',
        controller: jfBuildInfoJsonController,
        controllerAs: 'jfBuildInfoJson',
        scope: {
        },
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_build_info_json.html'
    }
}