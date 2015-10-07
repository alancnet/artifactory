package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.storage.db.aql.parser.AqlParser;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;

/**
 * @author Gidi Shabat
 */
public class DynamicStar extends DomainSensitiveParserElement {

    @Override
    protected ParserElement init() {
        switch (domain) {
            case items: {
                return AqlParser.itemStar;
            }
            case archives: {
                return AqlParser.archiveStar;
            }
            case properties: {
                return AqlParser.propertiesStar;
            }
            case statistics: {
                return AqlParser.statisticsStar;
            }
            case artifacts: {
                return AqlParser.buildArtifactStar;
            }
            case dependencies: {
                return AqlParser.buildDependenciesStar;
            }
            case builds: {
                return AqlParser.buildStar;
            }
            case modules: {
                return AqlParser.buildModuleStar;
            }
            case moduleProperties: {
                return AqlParser.buildModulePropertiesStar;
            }
            case buildProperties: {
                return AqlParser.buildPropertiesStar;
            }
        }
        throw new UnsupportedOperationException("Unsupported domain :" + domain);
    }
}