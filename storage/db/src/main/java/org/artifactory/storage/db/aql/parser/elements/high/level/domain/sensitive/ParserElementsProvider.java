package org.artifactory.storage.db.aql.parser.elements.high.level.domain.sensitive;

import org.artifactory.aql.AqlParserException;
import org.artifactory.aql.model.AqlDomainEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gidi Shabat
 */
public class ParserElementsProvider {
    private Map<AqlDomainEnum, Map<Class, DomainSensitiveParserElement>> domainMap = new HashMap<>();

    public <T extends DomainSensitiveParserElement> T provide(Class<T> elementClass, AqlDomainEnum domain) {
        try {
            Map<Class, DomainSensitiveParserElement> map = domainMap.get(domain);
            if (map == null) {
                map = new HashMap<>();
                domainMap.put(domain, map);
            }
            T parserElement = (T) map.get(elementClass);
            if (parserElement == null) {

                parserElement = elementClass.newInstance();

                parserElement.setDomain(domain);
                parserElement.setProvider(this);
                map.put(elementClass, parserElement);
            }
            return parserElement;
        } catch (Exception e) {
            throw new AqlParserException("Fail to init the parser", e);
        }
    }
}
