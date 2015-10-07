package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class DynamicDomain extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: {
                return AqlParser.itemDomains;
            }
            case archives: {
                return AqlParser.archiveDomains;
            }
            case properties: {
                return AqlParser.propertiesDomains;
            }
            case statistics: {
                return AqlParser.statisticsDomains;
            }
            case artifacts: {
                return AqlParser.buildArtifactDomains;
            }
            case dependencies: {
                return AqlParser.buildDependenciesDomains;
            }
            case builds: {
                return AqlParser.buildDomains;
            }
            case modules: {
                return AqlParser.buildModuleDomains;
            }
            case moduleProperties: {
                return AqlParser.buildModulePropertiesDomains;
            }
            case buildProperties: {
                return AqlParser.buildPropertiesDomains;
            }
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}