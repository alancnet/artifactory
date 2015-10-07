package org.artifactory.converters;

import org.artifactory.common.property.ArtifactoryConverter;
import org.artifactory.version.CompoundVersionDetails;

/**
 * @author Gidi Shabat
 */
public interface ArtifactoryConverterAdapter extends ArtifactoryConverter {

    boolean isInterested(CompoundVersionDetails source, CompoundVersionDetails target);

    void revert();

    void backup();

    void clean();
}
