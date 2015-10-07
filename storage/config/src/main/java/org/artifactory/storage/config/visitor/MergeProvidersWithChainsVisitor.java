package org.artifactory.storage.config.visitor;


import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.ProviderMetaData;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class MergeProvidersWithChainsVisitor extends ConfigVisitor {


    @Override
    ProviderMetaData onProvider(ProviderMetaData providerMetaData) {
        return null;
    }

    @Override
    ChainMetaData onChain(ChainMetaData source) {
        return null;
    }

    @Override
    ProviderMetaData onChainSubProvider(ProviderMetaData providerMetaData) {
        ProviderMetaData defaultProviderMetaData = findDefaultProvider(providerMetaData.getId());
        if (defaultProviderMetaData != null) {
            providerMetaData.merge(defaultProviderMetaData);
            providerMetaData.setType(defaultProviderMetaData.getType());
        }
        return null;
    }

    @Override
    public ProviderMetaData onChainProvider(ProviderMetaData providerMetaData) {
        ProviderMetaData defaultProviderMetaData = findDefaultProvider(providerMetaData.getId());
        if (defaultProviderMetaData != null) {
            providerMetaData.merge(defaultProviderMetaData);
            providerMetaData.setType(defaultProviderMetaData.getType());
        }
        return null;
    }

    private ProviderMetaData findDefaultProvider(String identity) {
        List<ProviderMetaData> providerMetaDatas = getTargetConfig().getProviderMetaDatas();
        for (ProviderMetaData providerMetaData : providerMetaDatas) {
            if (providerMetaData.getId() == null) {
                throw new RuntimeException(
                        "Failed to load binaries provider config due  missing identity key on provider item");
            }
            if (providerMetaData.getId().equals(identity)) {
                return providerMetaData;
            }
        }
        return null;
    }
}
