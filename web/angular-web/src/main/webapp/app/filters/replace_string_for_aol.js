// TODO: pass in key to filter instead of the text
const dictionary = {
	'Import & Export': 'Import',
	'Repositories Import & Export': 'Import Repositories'
};

export function ReplaceStringForAol(ArtifactoryFeatures) {

    return function(str) {
    	if (ArtifactoryFeatures.isAol()) {
    		return dictionary[str] || str;
    	}
    	else {
    		return str;
    	}
    }
}