package org.artifactory.ui.rest.model.builds;

import java.util.List;

import org.artifactory.api.bintray.BintrayParams;
import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class BintrayModel extends BaseModel {

    private List<String> binTrayRepositories;
    private List<String> binTrayPackages;
    private List<String> binTrayVersions;
    private BintrayParams bintrayParams;


    public List<String> getBinTrayRepositories() {
        return binTrayRepositories;
    }

    public void setBinTrayRepositories(List<String> binTrayRepositories) {
        this.binTrayRepositories = binTrayRepositories;
    }

    public List<String> getBinTrayPackages() {
        return binTrayPackages;
    }

    public void setBinTrayPackages(List<String> binTrayPackages) {
        this.binTrayPackages = binTrayPackages;
    }

    public List<String> getBinTrayVersions() {
        return binTrayVersions;
    }

    public void setBinTrayVersions(List<String> binTrayVersions) {
        this.binTrayVersions = binTrayVersions;
    }

    public BintrayParams getBintrayParams() {
        return bintrayParams;
    }

    public void setBintrayParams(BintrayParams bintrayParams) {
        this.bintrayParams = bintrayParams;
    }
}
