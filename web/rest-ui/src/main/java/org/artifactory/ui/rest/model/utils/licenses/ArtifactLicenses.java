package org.artifactory.ui.rest.model.utils.licenses;

import java.util.List;

/**
 * @author Chen Keinan
 */
public class ArtifactLicenses {

    List<String> predefineValues;
    List<String> selectedValues;

    public List<String> getPredefineValues() {
        return predefineValues;
    }

    public void setPredefineValues(List<String> predefineValues) {
        this.predefineValues = predefineValues;
    }

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
    }
}
