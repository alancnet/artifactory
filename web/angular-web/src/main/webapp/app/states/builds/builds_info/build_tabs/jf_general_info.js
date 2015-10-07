import DICTIONARY from './../../constants/builds.constants';

class jfGeneralInfoController {
    constructor($scope, $stateParams, BuildsDao, PushToBintrayModal, User) {
        this.$scope = $scope;
        this.$stateParams = $stateParams;
        this.buildsDao = BuildsDao;
        this.pushToBintrayModal = PushToBintrayModal;
        this.generalData = {};
        this.DICTIONARY = DICTIONARY.generalInfo;

        this.userCanPushToBintray = User.getCurrent().canPushToBintray();

        this._getGeneralInfo();
    }

    pushToBintray() {
        this.modalInstance = this.pushToBintrayModal.launchModal('build');
    }

    _getGeneralInfo() {
        return this.buildsDao.getData({
            name: this.$stateParams.buildName,
            number: this.$stateParams.buildNumber,
            time: this.$stateParams.startTime,
            action:'buildInfo'
        }).$promise.then((data) => {
            this.generalData = data;
        });

    }

}


export function jfGeneralInfo() {
    return {
        restrict: 'EA',
        controller: jfGeneralInfoController,
        controllerAs: 'jfGeneralInfo',
        scope: {},
        bindToController: true,
        templateUrl: 'states/builds/builds_info/build_tabs/jf_general_info.html'
    }
}