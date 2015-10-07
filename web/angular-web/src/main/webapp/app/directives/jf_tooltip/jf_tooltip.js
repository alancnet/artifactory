export function jfTooltip() {
    return {
        restrict: 'A',
        link: function($scope, $element, $attrs) {
            let content = $attrs.jfTooltip;

            $($element).tooltipster({
                contentAsHTML : 'true',
                trigger: 'hover',
                onlyOne: 'true',
                interactive: 'true',
                position: 'bottom',
                content: content
            });

            $attrs.$observe('jfTooltip', function(val){
                $($element).tooltipster('content', val);
            });
        }
    }
}