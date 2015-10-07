package org.artifactory.converters.helpers;

import org.artifactory.descriptor.config.CentralConfigDescriptor;
import org.artifactory.state.ArtifactoryServerState;
import org.artifactory.state.model.ArtifactoryStateManager;
import org.artifactory.version.CompoundVersionDetails;

/**
 * Author: gidis
 */
public class MockArtifactoryStateManager implements ArtifactoryStateManager {

    @Override
    public boolean forceState(ArtifactoryServerState state) {
        return false;
    }

    @Override
    public void beforeDestroy() {
    }

    @Override
    public void init() {
    }

    @Override
    public void reload(CentralConfigDescriptor oldDescriptor) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void convert(CompoundVersionDetails source, CompoundVersionDetails target) {
    }

}
