package org.artifactory.storage.config;


import org.artifactory.storage.config.model.ChainMetaData;
import org.artifactory.storage.config.model.Config;
import org.artifactory.storage.config.model.ProviderMetaData;
import org.artifactory.storage.config.visitor.ConfigExplorer;
import org.artifactory.storage.config.visitor.MergeProvidersWithChainsVisitor;
import org.artifactory.storage.config.visitor.MergeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.List;

/**
 * @author Gidi Shabat
 */
public class BinaryProviderConfigBuilder {
    public static final String USER_TEMPLATE = "user-chain";
    private static final Logger log = LoggerFactory.getLogger(BinaryProviderConfigBuilder.class);

    /**
     * This method should be removed after converting the old storage.properties config to the new generation config
     * It is kind of hack to support the old generation.
     */
    public static ChainMetaData buildUserTemplate(InputStream defaultConfigStream, String userTemplate) {
        try {
            log.debug("Loading the default config");
            // Load user and default configs
            Config defaultConfig = loadConfig(defaultConfigStream);
            // Create config model explorer
            ConfigExplorer explorer = new ConfigExplorer();
            // Merge main providers with main chain providers
            log.debug("Merging config providers metadata with the chain providers metadata");
            explorer.visit(defaultConfig, defaultConfig, new MergeProvidersWithChainsVisitor());
            // Find Selected chain
            log.debug("Selecting the default chain :" + userTemplate);
            ChainMetaData selectedChain = findSelectedChain(userTemplate, defaultConfig);
            // enrich with first provider: UserTrackingBinaryProvider
            log.debug("Adding UserTrackingBinaryProvider at the beginning of the chain");
            enrichWithFirstProvider(selectedChain);
            return selectedChain;
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to load binary provider config. Reason invalid XML", e);
        }
    }

    /**
     * The method build binary providers chain using info from the binarystore.xml (new generation)
     */
    public static ChainMetaData buildByUserConfig(InputStream defaultConfigStream, InputStream userConfigStream) {
        try {
            log.debug("Loading the default config");
            // Load user and default configs
            Config defaultConfig = loadConfig(defaultConfigStream);
            log.debug("Loading the user config");
            Config userConfig = loadConfig(userConfigStream);
            // Load and merge user config and default config
            log.debug("Merging config providers metadata with the chain providers metadata");
            ConfigExplorer explorer = new ConfigExplorer();
            // Make sure that the user config is valid
            validateUserConfig(userConfig);
            // Add template if user created chain
            addTemplateAndIdentityIfUserCreatChain(userConfig);
            // Merge user config with main config
            explorer.visit(userConfig, defaultConfig, new MergeVisitor());
            // Merge main providers with main chain providers
            explorer.visit(defaultConfig, defaultConfig, new MergeProvidersWithChainsVisitor());
            Config config = defaultConfig;
            // Find Selected chain
            String userTemplate = userConfig.getChains().get(0).getTemplate();
            log.debug("Selecting the default chain :" + userTemplate);
            ChainMetaData selectedChain = findSelectedChain(userTemplate, config);
            // enrich with first provider: UserTrackingBinaryProvider
            log.debug("Adding UserTrackingBinaryProvider at the beginning of the chain");
            enrichWithFirstProvider(selectedChain);
            return selectedChain;
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to load binary provider config. Reason invalid XML", e);
        }
    }

    private static void enrichWithFirstProvider(ChainMetaData chain) {
        ProviderMetaData providerMetaData = new ProviderMetaData("tracking", "tracking");
        providerMetaData.setProviderMetaData(chain.getProviderMetaData());
        chain.setProviderMetaData(providerMetaData);
    }

    private static void addTemplateAndIdentityIfUserCreatChain(Config userConfig) {
        ChainMetaData chain = userConfig.getChains().get(0);
        String template = chain.getTemplate();
        if (isEmpty(template)) {
            // Set the default template
            chain.setTemplate(USER_TEMPLATE);
        }
    }

    private static boolean isEmpty(String template) {
        return template == null || template.trim().length() == 0;
    }

    private static void validateUserConfig(Config userConfig) {
        List<ChainMetaData> chains = userConfig.getChains();
        if (chains.size() != 1) {
            throw new RuntimeException(
                    "Failed to load binary provider config. Reason only one chain definition is allowed in binarystore.xml ");
        }
        ChainMetaData chain = chains.get(0);
        if (!isEmpty(chain.getTemplate()) && chain.getProviderMetaData() != null) {
            throw new RuntimeException(
                    "Failed to load binary provider config. Reason when defining user-chain the template field should not be used");
        }
        if (isEmpty(chain.getTemplate()) && chain.getProviderMetaData() == null) {
            throw new RuntimeException(
                    "Failed to load binary provider config. Reason a user chain should be either template or chain definition");
        }
    }

    private static ChainMetaData findSelectedChain(String userTemplate, Config config) {
        List<ChainMetaData> chains = config.getChains();
        for (ChainMetaData chain : chains) {
            if (chain.getTemplate().equals(userTemplate)) {
                return chain;
            }
        }
        return null;
    }

    private static Config loadConfig(InputStream stream) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        Config config = (Config) jaxbUnmarshaller.unmarshal(stream);
        if (isEmpty(config.getVersion())) {
            throw new RuntimeException(
                    "Failed to load binary provider config. Reason the config file should contain version");
        }
        return config;
    }
}
