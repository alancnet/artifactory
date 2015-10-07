import TOOLTIP from '../../../../constants/artifact_tooltip.constant';

export class AdminConfigurationBlack_duckController {

    constructor(BlackDuckDao, ProxiesDao) {
        this.blackDuckDao = BlackDuckDao.getInstance();
        this.proxiesDao = ProxiesDao;
        this.TOOLTIP = TOOLTIP.admin.configuration.blackDuck;
        this._initBlackDuck();
    }

    _initBlackDuck() {
        this.proxiesDao.get().$promise.then((proxies)=> {
            this.proxies = [''];
            this.proxies = this.proxies.concat(proxies);
            this.getBlackduckData();
        })
    }

    getBlackduckData() {
        this.blackDuckDao.get().$promise.then((data)=> {
//            console.log(data);
            this.blackDuck = data;
        });
    }

    save(duck) {
        if (duck.proxyRef==='') delete duck.proxyRef;
        this.blackDuckDao.update(duck);
    }

    testBlackDuck(duck) {
        this.blackDuckDao.save(duck).$promise.then(function (data) {
//            console.log(data);
        });
    }

    reset(){
        this.getBlackduckData();
    }
}