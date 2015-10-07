package org.artifactory.ui.rest.model.home;

import org.artifactory.addon.AddonInfo;
import org.artifactory.addon.AddonState;
import org.artifactory.addon.AddonType;
import org.artifactory.rest.common.model.BaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chen Keinan
 */
public class AddonModel extends BaseModel {

    private String name;
    private String configurationUrl;
    private String learnMoreUrl;
    private String status;
    private String[] categories;
    private String type;
    private String displayName;

    public AddonModel(AddonType addonType, AddonInfo addonInfo, String url, String configurationUrl) {
        this.learnMoreUrl = url;
        updateAddonState(addonInfo);
        setCategoriesData(addonType);
        this.type = addonType.getType();
        this.setName(addonType.getAddonName());
        this.setDisplayName(addonType.getAddonDisplayName());
        this.configurationUrl = configurationUrl;
    }

    /**
     * set categories data
     *
     * @param addonType - addon type (docker,aql,build and etc)
     */
    private void setCategoriesData(AddonType addonType) {
        if (status.equals("Available")) {
            List<String> categoriesList = new ArrayList<>();
            if (addonType.getCategories() != null && addonType.getCategories().length > 0) {
                for (String catgor : addonType.getCategories()) {
                    categoriesList.add(catgor);
                }
                categoriesList.add("available");
                categories = categoriesList.toArray(new String[categoriesList.size()]);
                ;
            }
        } else {
            this.categories = addonType.getCategories();
        }
    }

    private void updateAddonState(AddonInfo addonInfo) {
        if (addonInfo != null) {
            this.status = getAddonStatus(addonInfo.getAddonState());
        } else {
            this.status = AddonState.INACTIVATED.name();
        }
    }


    public String getConfigurationUrl() {
        return configurationUrl;
    }

    public void setConfigurationUrl(String configurationUrl) {
        this.configurationUrl = configurationUrl;
    }

    public String getLearnMoreUrl() {
        return learnMoreUrl;
    }

    public void setLearnMoreUrl(String learnMoreUrl) {
        this.learnMoreUrl = learnMoreUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getType() {
        return type;
    }

    private String getAddonStatus(AddonState addonState) {
        switch (addonState) {
            case NOT_CONFIGURED:    // Fall-through
            case ACTIVATED:
                return "Available";
            case DISABLED:
                return "Disabled";
            case INACTIVATED:   // Fall-through
            case NOT_LICENSED:  // Fall-through
            default:
                return "Not Available";
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
