class jfRevealInputController {
	updateInput() {
		let input = $(`#${this.inputId}`);
		let type = input.attr('type');
		if (type === 'text') {
			input.attr('type', 'password');
		}
		else {
			input.attr('type', 'text');
		}
	}
}

export function jfRevealInput() {
    return {
    	restrict: 'A',
		template: `<i class="icon icon-view icon-2x jf-reveal-input"
		   			  ng-click="jfRevealInput.updateInput()"></i>`,
		controller: jfRevealInputController,
		controllerAs: 'jfRevealInput',
		bindToController: true,
		scope: {
			inputId: '@jfRevealInput'
		}
    }
}