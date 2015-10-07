import EVENTS from '../../constants/common_events.constants';
class jfClearErrorsController {

    constructor($scope, $element, $attrs, ArtifactoryEventBus) {

        this.artifactoryEventBus = ArtifactoryEventBus;

        angular.element($element).on("mousedown",()=>{
            this.clearFieldValidations();
        })
    }

    clearFieldValidations() {
        this.artifactoryEventBus.dispatch(EVENTS.FORM_CLEAR_FIELD_VALIDATION, true);
    }

}

export function jfClearErrors() {

    return {
        restrict: 'A',
        controller: jfClearErrorsController
    };
}
