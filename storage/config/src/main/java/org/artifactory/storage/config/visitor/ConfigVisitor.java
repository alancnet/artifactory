package org.artifactory.storage.config.visitor;


import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.Config;
import org.artifactory.storage.config.model.ProviderMetaData;

/**
 * @author Gidi Shabat
 */
public abstract class ConfigVisitor {
    private Config targetConfig;

    abstract ProviderMetaData onProvider(ProviderMetaData providerMetaData);

    abstract ChainMetaData onChain(ChainMetaData source);

    abstract ProviderMetaData onChainSubProvider(ProviderMetaData providerMetaData);

    abstract ProviderMetaData onChainProvider(ProviderMetaData providerMetaData);

    public Config getTargetConfig() {
        return targetConfig;
    }

    public void setTargetConfig(Config targetConfig) {
        this.targetConfig = targetConfig;
    }

    protected boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }
}
