export class HomeController {
    constructor(HomePageDao, $scope, $timeout, User) {
        this.$scope = $scope;
        this.$timeout = $timeout;
        this.userService = User;
        this.offlineMode = this.userService.getCurrent().offlineMode;

        this.homePageDao = HomePageDao;
        this.homepageData = {};
        this.allAddons = {};
        this.addons = {};
        this.initHomePage();

        this.tabOptions = ['All', 'Package Management', 'Features', 'Ecosystem', 'Available'];
        this.currentType = this.tabOptions[0];

        this.showNews = false;
    }

    initHomePage() {
        if (!this.offlineMode) this.readUpdateHTML();
        this.homePageDao.get().$promise.then((data)=> {
            this.homepageData = data;
            this.allAddons = data.addons;

            this.sortByCurrentType();
        });
    }

    animateAddons() {
        var count = 0, animationInterval = 30
        $(".addon-icon")
                .removeClass('swelling')
                .each(function() {
                    setTimeout(function() {
                        $(this).addClass('swelling')
                    }.bind(this), count)
                    count += animationInterval
                })
    }

    sortByCurrentType() {
        this.addons = _.filter(this.allAddons, (addon)=> {
            return addon.categories.indexOf(this._camelize(this.currentType)) !== -1;
        });
        $(".addon-icon")
                .removeClass('swelling')

        // Commented out until we get a clearance from Yoav
        // setTimeout(this.animateAddons.bind(this),100)
    }

    _camelize(str) {
        return str.replace(/(?:^\w|[A-Z]|\b\w)/g, function (letter, index) {
            return index == 0 ? letter.toLowerCase() : letter.toUpperCase();
        }).replace(/\s+/g, '');
    }

    readUpdateHTML() {
        var xhr = new XMLHttpRequest();
        xhr.open('GET', 'https://service.jfrog.org/artifactory/updatesv4', true);
        xhr.onreadystatechange= ()=>{
            this.updateHTML=xhr.response;
            this.$scope.$apply();

            //twitter button javascript !
            !function(d,s,id){
                var js,fjs=d.getElementsByTagName(s)[0],p=/^http:/.test(d.location)?'http':'https';
                if(!d.getElementById(id)){
                    js=d.createElement(s);
                    js.id=id;js.src=p+'://platform.twitter.com/widgets.js';
                    fjs.parentNode.insertBefore(js,fjs);
                }
            }(document, 'script', 'twitter-wjs');

            this.$scope.$on('$destroy', () => {
                let twitter = document.getElementById('twitter-wjs');
                if (twitter) twitter.remove();
            });

            if(xhr.response) {
                this.$timeout(()=>{
                    this.showNews = true;
                },200);
            }
        };
        xhr.send();
    }

}