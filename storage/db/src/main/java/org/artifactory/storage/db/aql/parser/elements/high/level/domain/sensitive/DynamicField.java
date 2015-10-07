package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class DynamicField extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: {
                return AqlParser.itemFields;
            }
            case archives: {
                return AqlParser.archiveFields;
            }
            case properties: {
                return AqlParser.propertiesFields;
            }
            case statistics: {
                return AqlParser.statisticsFields;
            }
            case artifacts: {
                return AqlParser.buildArtifactFields;
            }
            case dependencies: {
                return AqlParser.buildDependenciesFields;
            }
            case builds: {
                return AqlParser.buildFields;
            }
            case modules: {
                return AqlParser.buildModuleFields;
            }
            case moduleProperties: {
                return AqlParser.buildModulePropertiesFields;
            }
            case buildProperties: {
                return AqlParser.buildPropertiesFields;
            }
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}
