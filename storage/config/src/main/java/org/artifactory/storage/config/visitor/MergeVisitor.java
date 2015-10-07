package org.artifactory.storage.config.visitor;

import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.ProviderMetaData;

import java.util.List;

import static org.artifactory.storage.config.BinaryProviderConfigBuilder.USER_TEMPLATE;

/**
 * @author Gidi Shabat
 */
public class MergeVisitor extends ConfigVisitor {

    @Override
    public ProviderMetaData onProvider(ProviderMetaData providerMetaData) {
        ProviderMetaData existingProviderMetaData = findProvider(providerMetaData.getId());
        if (existingProviderMetaData == null) {
            return new ProviderMetaData(providerMetaData);
        } else {
            if (!existingProviderMetaData.getType().equals(providerMetaData.getType())) {
                throw new RuntimeException("Fail to build binary provider config. Reason conflict between two" +
                        " providers with the same id: '" + existingProviderMetaData.getId() +
                        "' but with different types: '" + existingProviderMetaData.getType() + "' and : '" + providerMetaData.getType() + "'");
            }
            existingProviderMetaData.merge(providerMetaData);
        }
        return null;
    }

    @Override
    public ChainMetaData onChain(ChainMetaData source) {
        // Allow only to add new chains, merger is not allowed
        if (USER_TEMPLATE.equals(source.getTemplate())) {
            return new ChainMetaData(source);
        }
        ChainMetaData oldChain = findChainsByTemplate(source.getTemplate());
        if (oldChain == null) {
            throw new RuntimeException(
                    "Fail to build binary provider config. Reason couldn't find chain with id: " + source.getTemplate());
        } else {
            oldChain.setTemplate(source.getTemplate());
        }
        return null;
    }

    @Override
    public ProviderMetaData onChainSubProvider(ProviderMetaData providerMetaData) {
        // This is chain internal element therefore no merging is allowed (it must be new element)
        ProviderMetaData newProviderMetaData = new ProviderMetaData(providerMetaData);
        newProviderMetaData.merge(providerMetaData);
        return newProviderMetaData;
    }

    @Override
    public ProviderMetaData onChainProvider(ProviderMetaData providerMetaData) {
        // This is chain internal element therefore no merging is allowed (it must be new element)
        ProviderMetaData newProviderMetaData = new ProviderMetaData(providerMetaData);
        newProviderMetaData.merge(providerMetaData);
        return newProviderMetaData;
    }

    private ChainMetaData findChainsByTemplate(String template) {
        List<ChainMetaData> chains = getTargetConfig().getChains();
        for (ChainMetaData chain : chains) {
            if (template != null && template.equals(chain.getTemplate())) {
                return chain;
            }
        }
        return null;
    }

    private ProviderMetaData findProvider(String id) {
        List<ProviderMetaData> providerMetaDatas = getTargetConfig().getProviderMetaDatas();
        for (ProviderMetaData providerMetaData : providerMetaDatas) {
            if (isEmpty(providerMetaData.getId()) || isEmpty(providerMetaData.getType())) {
                throw new RuntimeException(
                        "Failed to load binaries provider config. Reason: missing identity or type in provider item.");
            }
            if (providerMetaData.getId().equals(id)) {
                return providerMetaData;
            }
        }
        return null;
    }
}
