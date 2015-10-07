export class AdminAdvancedSystemInfoController {
    constructor(SystemInfoDao) {
       // console.log("log: "+SystemInfoDao);
        this.systemInfoDao = SystemInfoDao.getInstance();
        let self=this;
        this.systemInfo;
        this.systemInfoJoined;
        this.systemInfoDao.get().$promise.then(function(data){
            self.getSystemInfoKeys(data);
        })
    }
    getSystemInfoKeys(data) {
        this.systemInfo = data.systemInfo;
        this.systemInfoJoined = JSON.stringify(data.systemInfo);
        //let headers = Object.keys(systemInfo);
        //let subTitles = [];
    }

    replaceNewLines(text) {
        return text.replace(/\n/g, "<br>");
    }
}