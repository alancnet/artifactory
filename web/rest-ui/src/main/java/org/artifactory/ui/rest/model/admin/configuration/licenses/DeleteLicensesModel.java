package org.artifactory.ui.rest.model.admin.configuration.licenses;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteLicensesModel implements RestModel {
    private List<String> licenseskeys = Lists.newArrayList();

    public List<String> getLicenseskeys() {
        return licenseskeys;
    }

    public void addLicenseKey(String licenseKey) {
        licenseskeys.add(licenseKey);
    }
}
