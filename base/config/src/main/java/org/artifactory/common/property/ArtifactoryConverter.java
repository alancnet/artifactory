package org.artifactory.common.property;

import org.artifactory.version.CompoundVersionDetails;

/**
 * Author: gidis
 */
public interface ArtifactoryConverter {
    /**
     * Run any necessary conversions to bring the system from the source version to the target version.
     * This method is called before {@link org.artifactory.spring.ReloadableBean#init()} and in the order of
     * dependencies between the services as declared in {@link org.artifactory.spring.Reloadable#initAfter()}.
     */
    void convert(CompoundVersionDetails source, CompoundVersionDetails target);
}
