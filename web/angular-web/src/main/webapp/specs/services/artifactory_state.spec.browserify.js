/**
 * Created by idannaim on 8/2/15.
 */
let artifactoryState;

describe('Unit test: ArtifactoryState', () => {
    let mockState = {
        name: 'test',
        state: 'myState'
    };
    beforeEach(m('artifactory.services'));
    beforeEach(inject((ArtifactoryState) => {
        artifactoryState = ArtifactoryState;
    }));

    it('should save new state', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);

        expect(artifactoryState.getState(mockState.name)).toEqual(mockState.state);
    });

    it('should remove saved state', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);
        artifactoryState.removeState(mockState.name);
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });

    it('should check if get undefined not get error', ()=> {
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });

    it('should remove all saved states', ()=> {
        artifactoryState.setState(mockState.name, mockState.state);
        artifactoryState.clearAll();
        expect(artifactoryState.getState(mockState.name)).not.toBeDefined();
    });
});

