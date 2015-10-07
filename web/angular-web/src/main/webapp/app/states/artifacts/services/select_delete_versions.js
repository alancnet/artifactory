'use strict';
/**
 * launch a modal that prompts the user to select a target repo & path to do move / copy
 *
 * @param action:String - either 'copy' or 'move'
 * @returns promise - resolved with Object({targetRepoKey: String, targetPath: String}) if the user confirmed, rejected otherwise
 */
export function selectDeleteVersionsFactory(ArtifactActionsDao, $rootScope, ArtifactoryModal, ArtifactoryGridFactory) {
    return function selectDeleteVersions(node) {
        let modalInstance;
        let modalScope = $rootScope.$new();
        modalScope.noData = false;

        // Grid
        modalScope.versionsGridOptions = ArtifactoryGridFactory
                .getGridInstance(modalScope)
                .setColumns(
                [
                    {
                        name: 'Group ID',
                        displayName: 'Group ID',
                        field: 'groupId'
                    },
                    {
                        name: 'Version',
                        displayName: 'Version',
                        field: 'version'
                    },
                    {
                        name: 'Directories Count',
                        displayName: 'Directories Count',
                        field: 'directoriesCount'
                    }
                ])
                .setRowTemplate('default')
                .setMultiSelect();

        ArtifactActionsDao.getDeleteVersions({repoKey: node.data.repoKey, path: node.data.path})
                .$promise.then((versions) => {
                    modalScope.versions = versions.data.versions;
                    modalScope.versionsGridOptions.setGridData(versions.data.versions);
                    if (versions.data.versions.length == 0) {
                        modalScope.noData = true;
                    }
                });

        // Scope functions
        modalScope.selectedVersions = () => {
            return (modalScope.versions && modalScope.versionsGridOptions.api) && modalScope.versionsGridOptions.api.selection.getSelectedRows() || [];
        };

        modalScope.close = (version) => {
            modalInstance.close(version);
        };

        // Launch modal
        modalInstance = ArtifactoryModal.launchModal('select_delete_versions', modalScope);
        return modalInstance.result;
    }
}
