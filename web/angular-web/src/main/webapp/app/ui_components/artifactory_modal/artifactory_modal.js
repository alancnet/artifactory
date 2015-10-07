
import EVENTS     from '../../constants/artifacts_events.constants';

/**
 * @desc wrapper around the $modal service
 * @url http://angular-ui.github.io/bootstrap/#/modal
 */
export class ArtifactoryModal {

    constructor($modal, $rootScope, $q, $sce, ArtifactoryEventBus) {
        this.modal = $modal;
        this.$rootScope = $rootScope;
        this.$q = $q;
        this.$sce = $sce;
        this.templatesBaseUrl = 'ui_components/artifactory_modal/templates/';
        this.artifactoryEventBus = ArtifactoryEventBus;



    }

    /**
     * build the path to the template location
     * and delegate to the $modal service
     * return the modal instance
     *
     * @param template
     * @param scope
     * @returns {{Modal instance}}
     */
    launchModal(template, scope, size) {
        if (!size) size = 'lg';

        let templateUrl = this.templatesBaseUrl + template + '.html';
        let modalInstance =  this.modal.open({
            templateUrl: templateUrl,
            scope: scope,
            size: size
        });
        this.artifactoryEventBus.registerOnScope(this.$rootScope, EVENTS.CLOSE_MODAL, () => {
            modalInstance.dismiss();
        });

        return modalInstance;
    }

    /**
     * launch a modal that shows content in a codemirror container
     *
     * @param title - title of the modal
     * @param content - content for the code mirror container
     * @param mode - mode for code mirror editor options (usually {name: <mimetype>}
     * @returns {{Modal instance}}
     */
    launchCodeModal(title, content, mode) {
        let modalInstance;
        let modalScope = this.$rootScope.$new();
        modalScope.closeModal = () => modalInstance.close();
        modalScope.content = content;
        modalScope.mode = mode;
        modalScope.title = title;
        return modalInstance = this.launchModal('code_modal', modalScope);
    }


    /**
     * launch a modal that shows a confirmation box, and returns a promise
     *
     * @param title - title of the confirmation box
     * @param content - HTML content of the confirmation box
     * @param buttons - button text (Object(confirm: String, cancel: String))
     * @returns promise - resolved if the user confirmed, rejected otherwise
     */
    confirm(content, title, buttons) {
        buttons = buttons || {};
        buttons.confirm = buttons.confirm || 'Confirm';
        buttons.cancel = buttons.cancel || 'Cancel';
        title = title || 'Are you sure?';

        let modalInstance;
        let modalScope = this.$rootScope.$new();

        modalScope.buttons = buttons;
        modalScope.content = this.$sce.trustAsHtml(content);
        modalScope.title = title;
        return this.launchModal('confirm_modal', modalScope, 'sm').result;
    }

}

