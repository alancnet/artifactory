import API from '../../../../constants/api.constants';

export class AdminAdvancedSystemLogsController {
    constructor($scope, SystemLogsDao, $interval, $window, $timeout) {

        this.logsDao = SystemLogsDao;
        this.$interval = $interval;
        this.$window = $window;
        this.$timeout = $timeout;

        this.intervalPromise = null;
        this.timeoutSpinner = null;
        this.timeCount = 5;

        this._getInitialData();

        $scope.$on('$destroy', ()=> {
            this.stopTimeout();
            this.stopInterval();
        });
    }

    _getInitialData() {
        this.logsDao.getLogs().$promise.then((data)=> {
            this.refreshRateSecs = data.refreshRateSecs;
            this.logs = _.map(data.logs, (logName)=>{return {logName:logName}});
            this.selectedLog = this.logs[0].logName;
            this.data = {fileSize: 0};
            this._getLogData();
        });
    }

    _getLogData() {
        this.stopInterval();

        this.logsDao.getLogData({id: this.selectedLog, fileSize: this.data.fileSize, $no_spinner: true}).$promise.then((data)=> {
            this.stopTimeout();

            if (this.data.fileSize === 0) {
                this.$timeout(()=> {
                    var textarea = document.getElementById('textarea');
                    textarea.scrollTop = textarea.scrollHeight;
                });
            }

            if (data.fileSize)
                this.data = data;

            this.timeCount = this.refreshRateSecs;
            if (!this.intervalPromise)
                this.startInterval();
        });

        this.timeoutSpinner = this.$timeout(() => {
            this.timeCount--;
        }, 400);
    }

    download() {
        this.$window.open(`${API.API_URL}/systemlogs/downloadFile?id=`+this.selectedLog, '_blank');
    }


    onChangeLog() {
        this.stopInterval();
        this.data = {fileSize: 0};
        this._getLogData();
    }

    startInterval() {
        this.intervalPromise = this.$interval(()=> {
            if (this.timeCount == 0)
                this._getLogData();
            else
                this.timeCount--;
        }, 1000);
    }

    stopInterval() {
        if (this.intervalPromise) {
            this.$interval.cancel(this.intervalPromise);
            this.intervalPromise = null;
        }
    }

    stopTimeout() {
        if (this.timeoutSpinner) {
            this.$timeout.cancel(this.timeoutSpinner);
            this.timeoutSpinner = null;
        }
    }
}
