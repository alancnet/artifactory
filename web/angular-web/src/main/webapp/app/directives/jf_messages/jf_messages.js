class jfMessagesController {
    constructor($scope, $state, $window, ArtifactoryState) {
        this.$state = $state;
        this.artifactoryState = ArtifactoryState;
        this.$window = $window;

        angular.element(this.$window).on('resize', this.handleSizing.bind(this));
        $scope.$on('$destroy', () => {
            angular.element(this.$window).off('resize');
        });

        setTimeout(() => {
            this.handleSizing();

            $(document).on('mouseenter', '.message-text a', () => {
                $('.message-container').addClass('pause-animation')
            });
            $(document).on('mouseleave', '.message-text a', () => {
                $('.message-container').removeClass('pause-animation')
            });
        }, 300);
    }

    getConstantMessages() {
        return this.artifactoryState.getState('constantMessages');
    }

    getSystemMessage() {
        let msgObj = this.artifactoryState.getState('systemMessage');
        if (msgObj && msgObj.enabled && (msgObj.inAllPages || this.$state.current.name === 'home')) {
            this.systemMessage = msgObj;
            this.handleSizing();
        }
        else
            this.systemMessage = null;

        return this.systemMessage;
    }

    handleSizing() {
        if ($('.constant-message.system').length) {
            let maxMessageSize = this.$window.innerWidth - $('.constant-message.system .message-title').width() - ($('.constant-message.system .message-container').offset().left * 2) - 10,
                    msgText = $('.constant-message.system .message-text');

            if (msgText.find('span').width() > maxMessageSize)
                msgText.css('width', maxMessageSize).addClass('marqueed');
            else
                msgText.css('width', 'auto').removeClass('marqueed');
        }
    }
}

export function jfMessages() {
    return {
        controller: jfMessagesController,
        controllerAs: 'jfMessages',
        bindToController: true,
        templateUrl: 'directives/jf_messages/jf_messages.html'
    }
}
