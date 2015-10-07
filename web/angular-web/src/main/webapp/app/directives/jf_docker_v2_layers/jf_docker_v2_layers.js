import KEY_DICTIONARY from './details_key_dictionary.js';

class jfDockerV2LayersController {
    constructor($scope, $attrs) {
        this.KEY_DICTIONARY = KEY_DICTIONARY;

        this.selectedLayer = null;
        this.layerDirectives = [];

        this.controller.layersController = this;

        $('#jf-artifacts .main-view').on('scroll', () => {
            if (!this.layerDetailsTopOffset)
                this.layerDetailsTopOffset = $('.layer-details')[0].offsetTop + 20;

            let layerPanelTopOffset = $('.docker-layers-panel').position().top;
            if (layerPanelTopOffset < this.layerDetailsTopOffset * -1 && $('.layers-container').offset().top + $('.layers-container').height() + 30 > $(window).height())
                $('.layer-details').css('top',  Math.abs(layerPanelTopOffset) - this.layerDetailsTopOffset).css('position', 'absolute');
            else if ($('.layers-container').offset().top > 0)
                $('.layer-details').css('top',  0).css('position', 'relative');
        });

        $scope.$on('$destroy', () => {
            $('#jf-artifacts .main-view').off('scroll');
        });
    }

    setSelected(layer) {
        if (this.selectedLayer && this.selectedLayer !== layer)
            this.selectedLayer.setSelected(false);
        this.selectedLayer = layer;
    }

    refreshView() {
        if (this.layerDirectives.length)
            this.layerDirectives[0].setSelected(true);

        $('#jf-artifacts .main-view').animate({ scrollTop: "0" }, 400);
    }
}

export function jfDockerV2Layers() {
    return {
        restrict: 'E',
        scope: {
            controller: '=',
            data: '=',
            currentPath: '@'
        },
        controller: jfDockerV2LayersController,
        controllerAs: 'jfDockerV2Layers',
        templateUrl: 'directives/jf_docker_v2_layers/jf_docker_v2_layers.html',
        bindToController: true
    };
}
