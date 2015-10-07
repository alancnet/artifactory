package org.artifactory.storage.config.visitor;


import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.Config;
import org.artifactory.storage.config.model.ProviderMetaData;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class ConfigExplorer {

    public void visit(Config source, Config target, ConfigVisitor configVisitor) {
        configVisitor.setTargetConfig(target);
        List<ChainMetaData> chains = source.getChains();
        for (ChainMetaData chain : chains) {
            ChainMetaData result = configVisitor.onChain(chain);
            if (target != null && result != null) {
                target.getChains().add(result);
            }
            visit(chain, result, configVisitor);
        }
        List<ProviderMetaData> providerMetaDatas = source.getProviderMetaDatas();
        for (ProviderMetaData providerMetaData : providerMetaDatas) {
            if (providerMetaData.getId() == null) {
                throw new RuntimeException(
                        "Failed to load binaries provider config due  missing identity key on provider item");
            }
            ProviderMetaData result = configVisitor.onProvider(providerMetaData);
            if (target != null && result != null) {
                target.getProviderMetaDatas().add(result);
            }
        }
    }

    private void visit(ChainMetaData source, ChainMetaData target, ConfigVisitor configVisitor) {
        ProviderMetaData next = source.getProviderMetaData();
        if (next != null) {
            if (next.getId() == null) {
                throw new RuntimeException(
                        "Failed to load binaries provider config due to missing identity key on provider item");
            }
            ProviderMetaData result = configVisitor.onChainProvider(next);
            if (target != null && result != null) {
                target.setProviderMetaData(result);
            }
            visit(next, result, configVisitor);
        }
    }

    private void visit(ProviderMetaData source, ProviderMetaData target, ConfigVisitor configVisitor) {
        ProviderMetaData next = source.getProviderMetaData();
        ProviderMetaData result;
        if (next != null) {
            if (next.getId() == null) {
                throw new RuntimeException(
                        "Failed to load binaries provider config due to missing identity key on provider item");
            }
            result = configVisitor.onChainProvider(next);
            if (target != null && result != null) {
                target.setProviderMetaData(result);
            }
            visit(next, result, configVisitor);
        }
        List<ProviderMetaData> subProviderMetaDatas = source.getSubProviderMetaDataList();
        for (ProviderMetaData providerMetaData : subProviderMetaDatas) {
            if (providerMetaData.getId() == null) {
                throw new RuntimeException(
                        "Failed to load binaries provider config due to missing identity key on provider item");
            }
            result = configVisitor.onChainSubProvider(providerMetaData);
            if (target != null && result != null) {
                target.getSubProviderMetaDataList().add(result);
            }
            visit(providerMetaData, result, configVisitor);
        }
    }
}
