export function jfHideForAol() {
	return {
		restrict: 'A',
		controller: function(ArtifactoryFeatures, $element) {
			if (ArtifactoryFeatures.isAol()) {
				$($element).hide();
			}
		}
	}
}