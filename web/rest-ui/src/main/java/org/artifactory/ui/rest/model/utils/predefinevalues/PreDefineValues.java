package org.artifactory.ui.rest.model.utils.predefinevalues;

import java.util.List;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class PreDefineValues extends BaseModel {

    List<String> predefinedValues;
    List<String> selectedValues;

    public List<String> getSelectedValues() {
        return selectedValues;
    }

    public void setSelectedValues(List<String> selectedValues) {
        this.selectedValues = selectedValues;
    }

    public List<String> getPredefinedValues() {
        return predefinedValues;
    }

    public void setPredefinedValues(List<String> predefinedValues) {
        this.predefinedValues = predefinedValues;
    }
}
