package org.artifactory.descriptor.repo;

import org.artifactory.descriptor.Descriptor;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Yoav Luft
 */
@XmlType(name = "PypiConfigurationType", propOrder = {"indexContextPath", "packagesContextPath"})
public class PypiConfiguration implements Descriptor {

    @XmlElement(defaultValue = "", required = false)
    private String indexContextPath;

    @XmlElement(defaultValue = "", required = false)
    private String packagesContextPath;

    public String getPackagesContextPath() {
        return packagesContextPath;
    }

    public void setPackagesContextPath(@Nonnull String packagesContextPath) {
        this.packagesContextPath = packagesContextPath;
    }

    public String getIndexContextPath() {
        return indexContextPath;
    }

    public void setIndexContextPath(@Nonnull String indexContextPath) {
        this.indexContextPath = indexContextPath;
    }
}
