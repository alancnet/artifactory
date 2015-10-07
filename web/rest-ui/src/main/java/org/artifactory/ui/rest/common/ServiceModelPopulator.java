package org.artifactory.ui.rest.common;

import org.artifactory.descriptor.index.IndexerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.ui.rest.model.admin.services.indexer.Indexer;
import org.artifactory.ui.rest.model.empty.EmptyModel;

import javax.annotation.Nonnull;

/**
 * @author Chen Keinan
 */
public class ServiceModelPopulator {

    /**
     * populate indexer descriptor data to indexer model
     *
     * @param indexerDescriptor - indexer descriptor
     * @return indexer model
     */
    @Nonnull
    public static RestModel populateIndexerConfiguration(IndexerDescriptor indexerDescriptor) {
        if (indexerDescriptor != null) {
            return new Indexer(indexerDescriptor);
        }
        return new EmptyModel();
    }
}
