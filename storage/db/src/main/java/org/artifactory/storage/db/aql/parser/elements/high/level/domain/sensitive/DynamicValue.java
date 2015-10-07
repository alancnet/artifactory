package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class DynamicValue extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: {
                return AqlParser.itemValues;
            }
            case archives: {
                return AqlParser.archiveValues;
            }
            case properties: {
                return AqlParser.propertiesValues;
            }
            case statistics: {
                return AqlParser.statisticsValues;
            }
            case artifacts: {
                return AqlParser.buildArtifactValues;
            }
            case dependencies: {
                return AqlParser.buildDependenciesValues;
            }
            case builds: {
                return AqlParser.buildValues;
            }
            case modules: {
                return AqlParser.buildModuleValues;
            }
            case moduleProperties: {
                return AqlParser.buildModulePropertiesValues;
            }
            case buildProperties: {
                return AqlParser.buildPropertiesValues;
            }
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}
