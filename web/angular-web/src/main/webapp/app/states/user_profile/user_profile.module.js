import {UserProfileController} from "./user_profile.controller";

function config ($stateProvider) {

    $stateProvider
            .state('user_profile', {
                url: '/profile',
                parent: 'app-layout',
                templateUrl: 'states/user_profile/user_profile.html',
                controller: 'UserProfileController as UserProfile'
            })
}

export default angular.module('user_profile', [])
        .config(config)
        .controller('UserProfileController', UserProfileController)