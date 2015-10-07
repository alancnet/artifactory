/**
 * Created by idannaim on 8/3/15.
 */
let artifactoryModal, modal, scope;
let modalInstanceMock = {};
describe('unit test: ArtifactoryModal', ()=> {

    beforeEach(m('artifactory.services'));
    beforeEach(inject(($modal, $rootScope, ArtifactoryModal) => {
        artifactoryModal = ArtifactoryModal;
        modal = $modal;
        scope = $rootScope.$new();
        spyOn(modal, 'open').and.returnValue(modalInstanceMock);
    }));
    it('should open modal', ()=> {
        artifactoryModal.launchModal('user_modal', scope);
        expect(modal.open).toHaveBeenCalled();
    });
    it('should open modal', ()=> {
        let response = artifactoryModal.launchModal('user_modal', scope);
        expect(response).toBe(modalInstanceMock);
    });

    it('should set default size', ()=> {
        artifactoryModal.launchModal('user_modal', scope);
        let option = {
            templateUrl: 'ui_components/artifactory_modal/templates/user_modal.html',
            scope: scope,
            size: 'lg'
        };
        expect(modal.open).toHaveBeenCalledWith(option);
    });
});