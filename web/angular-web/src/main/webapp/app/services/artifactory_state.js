export class ArtifactoryState {
    constructor() {
        this.states = {};
    }

    getState(name) {
        return this.states[name];
    }

    setState(name, state) {
        this.states[name] = state;
    }

    removeState(name) {
        if (this.states[name]) delete this.states[name];
    }

    clearAll(){
        this.states = {};
    }

}