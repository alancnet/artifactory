export default function ($compile) {
    return {
        link: function ($scope, $element, attrs) {
            let newElement;
            let directiveName;
            newElement = $element.clone();
            directiveName = $scope.$eval(attrs.dynamicDirective);
            newElement.removeAttr('dynamic-directive');
            newElement.attr(directiveName, '');                       // assign the directive to it
            newElement = $compile(newElement)($scope);                // compile it
            $element.replaceWith(newElement);                         // replace the original element
        }
    }
};