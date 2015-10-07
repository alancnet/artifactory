import Grid     from './artifactory_grid/artifactory_grid.module';
import Modal    from './artifactory_modal/artifactory_modal.module';
import Uploader from './artifactory_uploader/artifactory_uploader.module'
angular.module('artifactory.ui_components', [
    Grid.name,
    Modal.name,
    Uploader.name
]);