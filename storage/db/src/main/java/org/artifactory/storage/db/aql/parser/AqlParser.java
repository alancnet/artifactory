package org.artifactory.storage.db.aql.parser;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.artifactory.aql.AqlParserException;
import org.artifactory.storage.db.aql.parser.elements.ParserElement;
import org.artifactory.storage.db.aql.parser.elements.high.level.basic.language.*;
import org.artifactory.storage.db.aql.parser.elements.low.level.InternalEmptyElement;

import java.util.Set;

/**
 * Parse the Aql string query into parser result which is a list of parser elements
 * The parser does not have state therefore a single instance can be used on single JVM
 *
 * @author Gidi Shabat
 */
public class AqlParser {
    public static final String[] DELIMITERS = {"<=", ">=", "!=", " ", "<", ">", "(", ")", "[", "]", "{", "}", "=", "'", ".", ":", "\"", ":"};
    public static final String[] USED_KEYS = {"$mt", "$lt", "$eq", "and", "not", "or", "artifacts"};
    public static final DotElement dot = new DotElement();
    public static final CommaElement comma = new CommaElement();
    public static final StarElement star = new StarElement();
    public static final QuotedComparator quotedComparator = new QuotedComparator();
    public static final ComparatorElement comparator = new ComparatorElement();
    public static final QuotesElement quotes = new QuotesElement();
    public static final ColonElement colon = new ColonElement();
    public static final ValueNumberNullElement valueOrNumberOrNull = new ValueNumberNullElement();
    public static final ValueElement value = new ValueElement();
    public static final NumberElement number = new NumberElement();
    public static final NullElement nullElement = new NullElement();
    public static final FieldElement field = new FieldElement();
    public static final OpenParenthesisElement openParenthesis = new OpenParenthesisElement();
    public static final CloseParenthesisElement closeParenthesis = new CloseParenthesisElement();
    public static final OpenCurlyBracketsElement openCurlyBrackets = new OpenCurlyBracketsElement();
    public static final CloseCurlyBracketsElement closedCurlyBrackets = new CloseCurlyBracketsElement();
    public static final OpenBracketsElement openBrackets = new OpenBracketsElement();
    public static final CloseBracketsElement closeBrackets = new CloseBracketsElement();
    public static final InternalEmptyElement empty = new InternalEmptyElement();
    public static final SortTypeElement sortType = new SortTypeElement();
    public static final LimitValueElement limitValue = new LimitValueElement();
    public static final OffsetValueElement offsetValue = new OffsetValueElement();

    public static final StatisticsDomainsElement statisticsDomains = new StatisticsDomainsElement();
    public static final BuildDomainsElement buildDomains = new BuildDomainsElement();
    public static final BuildPropertyDomainsElement buildPropertiesDomains = new BuildPropertyDomainsElement();
    public static final BuildModulePropertyDomainsElement buildModulePropertiesDomains = new BuildModulePropertyDomainsElement();
    public static final BuildModuleDomainsElement buildModuleDomains = new BuildModuleDomainsElement();
    public static final BuildDependenciesDomainsElement buildDependenciesDomains = new BuildDependenciesDomainsElement();
    public static final BuildArtifactDomainsElement buildArtifactDomains = new BuildArtifactDomainsElement();
    public static final PropertyDomainsElement propertiesDomains = new PropertyDomainsElement();
    public static final ArchiveDomainsElement archiveDomains = new ArchiveDomainsElement();
    public static final ItemDomainsElement itemDomains = new ItemDomainsElement();

    public static final StatisticsStarElement statisticsStar = new StatisticsStarElement();
    public static final BuildStarElement buildStar = new BuildStarElement();
    public static final BuildPropertyStarElement buildPropertiesStar = new BuildPropertyStarElement();
    public static final BuildModulePropertyStarElement buildModulePropertiesStar = new BuildModulePropertyStarElement();
    public static final BuildModuleStarElement buildModuleStar = new BuildModuleStarElement();
    public static final BuildDependenciesStarElement buildDependenciesStar = new BuildDependenciesStarElement();
    public static final BuildArtifactStarElement buildArtifactStar = new BuildArtifactStarElement();
    public static final PropertyStarElement propertiesStar = new PropertyStarElement();
    public static final ArchiveStarElement archiveStar = new ArchiveStarElement();
    public static final ItemStarElement itemStar = new ItemStarElement();

    public static final StatisticsValuesElement statisticsValues = new StatisticsValuesElement();
    public static final BuildValuesElement buildValues = new BuildValuesElement();
    public static final BuildPropertyValuesElement buildPropertiesValues = new BuildPropertyValuesElement();
    public static final BuildModulePropertyValuesElement buildModulePropertiesValues = new BuildModulePropertyValuesElement();
    public static final BuildModuleValuesElement buildModuleValues = new BuildModuleValuesElement();
    public static final BuildDependenciesValuesElement buildDependenciesValues = new BuildDependenciesValuesElement();
    public static final BuildArtifactValuesElement buildArtifactValues = new BuildArtifactValuesElement();
    public static final PropertyValuesElement propertiesValues = new PropertyValuesElement();
    public static final ArchiveValuesElement archiveValues = new ArchiveValuesElement();
    public static final ItemValuesElement itemValues = new ItemValuesElement();

    public static final StatisticsFieldsElement statisticsFields = new StatisticsFieldsElement();
    public static final BuildFieldsElement buildFields = new BuildFieldsElement();
    public static final BuildPropertyFieldsElement buildPropertiesFields = new BuildPropertyFieldsElement();
    public static final BuildModulePropertyFieldsElement buildModulePropertiesFields = new BuildModulePropertyFieldsElement();
    public static final BuildModuleFieldsElement buildModuleFields = new BuildModuleFieldsElement();
    public static final BuildDependenciesFieldsElement buildDependenciesFields = new BuildDependenciesFieldsElement();
    public static final BuildArtifactFieldsElement buildArtifactFields = new BuildArtifactFieldsElement();
    public static final PropertyFieldsElement propertiesFields = new PropertyFieldsElement();
    public static final ArchiveFieldsElement archiveFields = new ArchiveFieldsElement();
    public static final ItemFieldsElement itemFields = new ItemFieldsElement();

    public static final OffsetElement offset = new OffsetElement();
    public static final LimitElement limit = new LimitElement();
    public static final RootElement root = new RootElement();

    /**
     * Init once during the class initialisation.
     * All the parser instances will use the same parser elements instances
     */
    static {
        root.initialize();
    }

    /**
     * Initialize the parser process starting from the root element which represent the entire language
     *
     * @param query The AQL query string
     * @return Parsing result
     * @throws AqlParserException If query parsing fails
     */
    public ParserElementResultContainer parse(String query) {
        AqlParserContext parserContext = new AqlParserContext();
        ParserElementResultContainer[] parserElementResultContainers = root.peelOff(query, parserContext);
        for (ParserElementResultContainer parserElementResultContainer : parserElementResultContainers) {
            if (StringUtils.isBlank(parserElementResultContainer.getQueryRemainder())) {
                return parserElementResultContainer;
            }
        }
        String subQuery = parserContext.getQueryRemainder() != null ?
                parserContext.getQueryRemainder().trim() : query.trim();
        throw new AqlParserException(String.format("Fail to parse query: %s, it looks like there is syntax error near" +
                " the following sub-query: %s", query, subQuery));
    }

    /**
     * Initialize the parser process starting from the root element which represent the entire language and unlike the parse method
     * return the available possibilities to accept the next key word
     * This method is important for the new advance UI search.
     * <p/>
     * Examples:
     * 1. The String "items.find" will return "("
     * 2. The String "items.find(" will return {"(","\""}
     * 3. The String "items.find()" will return {"<empty>","."}
     * 3. The String "items.find()." will return {"sort","limit","include"}
     *
     * @param query The AQL query string
     * @return available possibilities to accept the next key word
     */
    public Set<String> predictNextKeyWord(String query) {
        AqlParserContext parserContext = new AqlParserContext();
        root.peelOff(query, parserContext);
        Set<String> result = Sets.newHashSet();
        for (ParserElement element : parserContext.getElements()) {
            for (String s : element.next()) {
                result.add(s);
}
        }
        System.out.println(result);
        return result;
    }
}


