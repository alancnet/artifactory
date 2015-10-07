import {HomeController} from "./home.controller";

function homeConfig ($stateProvider) {

    $stateProvider
            .state('home', {
                url: '/home',
                parent: 'app-layout',
                templateUrl: 'states/home/home.html',
                controller: 'HomeController as Home'
            })
}

export default angular.module('home', [])
        .config(homeConfig)
        .controller('HomeController', HomeController)