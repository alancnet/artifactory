export default function StateParamsMock(stateParams) {
  angular.mock.module(($provide) => {
    // mock the entire $state provider
    $provide.provider('$stateParams', () => {
        return {
            $get: () => { return stateParams}
        };
    });
  });
}
