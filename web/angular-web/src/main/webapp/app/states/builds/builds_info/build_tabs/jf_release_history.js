import DICTIONARY from './../../constants/builds.constants';

class jfReleaseHistoryController {
    constructor($stateParams, BuildsDao) {
        this.$stateParams = $stateParams;
        this.buildsDao = BuildsDao;
        this.historyData = null;
        this.DICTIONARY = DICTIONARY.releaseHistory;

        this._getData();
    }

    _getData() {
        return this.buildsDao.getDataArray({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action:'releaseHistory'
        }).$promise.then((data) => {
            this.historyData = data;                    
        }).catch(() => {
            this.historyData = [];
        });
    }
}

export function jfReleaseHistory() {
    return {
        restrict: 'EA',
        controller: jfReleaseHistoryController,
        controllerAs: 'jfReleaseHistory',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_release_history.html'
    }
}