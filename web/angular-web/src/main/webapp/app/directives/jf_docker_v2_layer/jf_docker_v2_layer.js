/*
 USAGE EXAMPLE:

 <jf-grid-filter
 filter-grid="gridOptions"  //the name of the grid (grid options)
 filter-field="fieldName"        //the name of the field that should be filtered
 filter-on-change>          //optional - don't use a button for filtering, filter on every change
 </jf-grid-filter>

 */
import COLOR_MAP from './cmd_color_map.js';

class jfDockerV2LayerController {

    constructor() {
        this.data.commandText = this.data.command + ' ' + this.data.commandText;
        this.selected = false;
        this.COLOR_MAP = COLOR_MAP;
        this.parent.layerDirectives.push(this);

        if (this.preselected)
            this.setSelected(true);
    }

    isSelected() {
        return this.selected;
    }

    setSelected(s) {
        this.selected = s;
        this.parent.setSelected(this);
    }
}

export function jfDockerV2Layer() {

    return {
        restrict: 'E',
        scope: {
            parent: '=',
            data: '=',
            preselected: '=',
            isBase: '=',
            isUpper: '='
        },
        controller: jfDockerV2LayerController,
        controllerAs: 'jfDockerV2Layer',
        templateUrl: 'directives/jf_docker_v2_layer/jf_docker_v2_layer.html',
        bindToController: true
    };
}
