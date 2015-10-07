import {ArtifactoryModal} from './artifactory_modal.js';
import {PushToBintrayModal} from './push_to_bintray_modal.js';

export default angular.module('artifactory_modal', ['ui.bootstrap'])
    .service('ArtifactoryModal', ArtifactoryModal)
    .service('PushToBintrayModal', PushToBintrayModal);