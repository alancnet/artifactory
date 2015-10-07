package org.artifactory.storage.db.aql.service.optimizer;

import com.google.common.collect.Lists;
import org.artifactory.aql.AqlFieldResolver;
import org.artifactory.aql.model.AqlComparatorEnum;
import org.artifactory.aql.model.AqlDomainEnum;
import org.artifactory.aql.model.AqlField;
import org.artifactory.aql.model.AqlFieldEnum;
import org.artifactory.aql.model.AqlItemTypeEnum;
import org.artifactory.aql.model.AqlValue;
import org.artifactory.aql.model.AqlVariable;
import org.artifactory.aql.model.AqlVariableTypeEnum;
import org.artifactory.storage.db.aql.sql.builder.links.TableLink;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlAdapter;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQuery;
import org.artifactory.storage.db.aql.sql.builder.query.aql.AqlQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.CloseParenthesisAqlElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.OpenParenthesisAqlElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.OperatorQueryElement;
import org.artifactory.storage.db.aql.sql.builder.query.aql.SimpleCriteria;
import org.artifactory.storage.db.aql.sql.builder.query.sql.SqlTable;
import org.artifactory.storage.db.aql.sql.builder.query.sql.type.AqlTableGraph;
import org.artifactory.storage.db.aql.sql.model.SqlTableEnum;

import java.util.ArrayList;
import java.util.List;

import static org.artifactory.aql.model.AqlFieldEnum.itemType;

/**
 * By default each node in the database can be or 0=folder or file=1, therefore a query with the file type = "all" is
 * being automatically replaced criteria with the following criterias :type=file or type=folder
 * ans since the sub query: type=file or type=folderis means all files it can be removed.
 *
 * @author Gidi Shabat
 */
public class FileTypeOptimization extends OptimizationStrategy {
    @Override
    public void optimize(AqlQuery aqlQuery, String transformation) {
        boolean change;
        do {
            // Remove Item type = all criterias
            change = removeUnnecessaryItemType(aqlQuery);
            // Remove duplicates operators
            change = change | removeDuplicateOperators(aqlQuery);
            // Remove last operator
            change = change | removeLastOperator(aqlQuery);
            // Remove last operator
            change = change | removeFirstOperator(aqlQuery);
            // Remove operator before close parenthesis
            change = change | removeOperatorBeforeCloseParenthesis(aqlQuery);
            // Remove operator after open parenthesis
            change = change | removeOperatorAfterOpenParenthesis(aqlQuery);
            // Remove parenthesis with operators
            change = change | removeEmptyParenthesisWithOperator(aqlQuery);
            // Remove empty parenthesis
            change = change | removeEmptyParenthesis(aqlQuery);
        } while (change);
    }

    private boolean removeUnnecessaryItemType(AqlQuery aqlQuery) {
        ItemTypeHandleEnum itemTypeInstances = findItemTypeInstances(aqlQuery);
        // No need for optimization
        if (ItemTypeHandleEnum.none == itemTypeInstances || ItemTypeHandleEnum.untuch == itemTypeInstances) {
            return false;
        }
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        int i;
        // Find the criteria that contains "any"
        for (i = 0; i < aqlElements.size(); i++) {
            AqlQueryElement aqlQueryElement = aqlElements.get(i);
            if (aqlQueryElement instanceof SimpleCriteria) {
                AqlField field = (AqlField) ((SimpleCriteria) aqlQueryElement).getVariable1();
                AqlValue value = (AqlValue) ((SimpleCriteria) aqlQueryElement).getVariable2();
                if (itemType == field.getFieldEnum() && ((Integer) value.toObject()) == AqlItemTypeEnum.any.type) {
                    break;
                }
            }
        }
        // Remove the criteria that contains "any"
        SimpleCriteria criteria = (SimpleCriteria) aqlElements.remove(i);
        // if we should replace it with the 'type=file or type=folder' sub query then do it
        if (ItemTypeHandleEnum.replace == itemTypeInstances) {
            List<AqlDomainEnum> subDomains = criteria.getSubDomains();
            AqlField itemType = AqlFieldResolver.resolve(AqlFieldEnum.itemType);
            AqlVariable files = AqlFieldResolver.resolve("file", AqlVariableTypeEnum.itemType);
            AqlVariable folders = AqlFieldResolver.resolve("folder", AqlVariableTypeEnum.itemType);
            TableLink tableLink = AqlTableGraph.tablesLinksMap.get(SqlTableEnum.nodes);
            SqlTable table = tableLink.getTable();
            SimpleCriteria criteria1 = new SimpleCriteria(subDomains, itemType, table,
                    AqlComparatorEnum.equals.signature, files, table);
            SimpleCriteria criteria2 = new SimpleCriteria(subDomains, itemType, table,
                    AqlComparatorEnum.equals.signature, folders, table);
            aqlElements.add(i, AqlAdapter.close);
            aqlElements.add(i, criteria2);
            aqlElements.add(i, AqlAdapter.or);
            aqlElements.add(i, criteria1);
            aqlElements.add(i, AqlAdapter.open);
        }
        return true;
    }

    private boolean removeOperatorBeforeCloseParenthesis(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        ArrayList<Integer> toRemove = Lists.newArrayList();
        AqlQueryElement prev = null;
        AqlQueryElement current;
        for (int i = 0; i < aqlElements.size(); i++) {
            current = aqlElements.get(i);
            if (current != null && prev != null) {
                if (prev.isOperator() && current instanceof CloseParenthesisAqlElement) {
                    // Invert the order, it will be easier to remove.
                    toRemove.add(0, i - 1);
                }
            }
            prev = current;
        }
        // Remove empty parenthesis from list
        for (Integer index : toRemove) {
            aqlElements.remove(index.intValue());
            change = true;
        }
        return change;
    }

    private boolean removeOperatorAfterOpenParenthesis(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        ArrayList<Integer> toRemove = Lists.newArrayList();
        AqlQueryElement prev = null;
        AqlQueryElement current;
        for (int i = 0; i < aqlElements.size(); i++) {
            current = aqlElements.get(i);
            if (current != null && prev != null) {
                if (prev instanceof OpenParenthesisAqlElement && current.isOperator()) {
                    // Invert the order, it will be easier to remove.
                    toRemove.add(0, i);
                }
            }
            prev = current;
        }
        // Remove empty parenthesis from list
        for (Integer index : toRemove) {
            aqlElements.remove(index.intValue());
            change = true;
        }
        return change;
    }

    private boolean removeEmptyParenthesis(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        AqlQueryElement prev = null;
        AqlQueryElement current;
        while (true) {
            ArrayList<Integer> toRemove = Lists.newArrayList();
            boolean found = false;
            for (int i = 0; i < aqlElements.size(); i++) {
                current = aqlElements.get(i);
                if (current != null && prev != null) {
                    if (prev instanceof OpenParenthesisAqlElement && current instanceof CloseParenthesisAqlElement) {
                        // Invert the order, it will be easier to remove.
                        toRemove.add(0, i - 1);
                        toRemove.add(0, i);
                        found = true;
                    }
                }
                prev = current;
            }
            if (!found) {
                break;
            }
            // Remove empty parenthesis from list
            for (Integer index : toRemove) {
                aqlElements.remove(index.intValue());
                change = true;
            }
        }
        return change;
    }

    private boolean removeEmptyParenthesisWithOperator(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        ArrayList<Integer> toRemove = Lists.newArrayList();
        AqlQueryElement first = null;
        AqlQueryElement second = null;
        AqlQueryElement third;
        for (int i = 0; i < aqlElements.size(); i++) {
            third = aqlElements.get(i);
            if (first != null && second != null && third != null) {
                if (first instanceof OpenParenthesisAqlElement && second instanceof OperatorQueryElement && third instanceof CloseParenthesisAqlElement) {
                    // Invert the order, it will be easier to remove.
                    toRemove.add(0, i - 1);
                }
            }
            first = second;
            second = third;
        }
        // Remove empty parenthesis from list
        for (Integer index : toRemove) {
            aqlElements.remove(index.intValue());
            change = true;
        }
        return change;
    }

    private boolean removeDuplicateOperators(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        ArrayList<Integer> toRemove = Lists.newArrayList();
        AqlQueryElement prev = null;
        AqlQueryElement current;
        for (int i = 0; i < aqlElements.size(); i++) {
            current = aqlElements.get(i);
            if (current != null && prev != null) {
                if (prev.isOperator() && current.isOperator()) {
                    // Invert the order, it will be easier to remove.
                    toRemove.add(0, i - 1);
                }
            }
            prev = current;
        }
        // Remove empty parenthesis from list
        for (Integer index : toRemove) {
            aqlElements.remove(index.intValue());
            change = true;
        }
        return change;
    }

    private boolean removeLastOperator(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        if (aqlElements.size() > 1) {
            AqlQueryElement aqlQueryElement = aqlElements.get(aqlElements.size() - 1);
            if (aqlQueryElement.isOperator()) {
                aqlElements.remove(aqlElements.size() - 1);
                change = true;
            }
        }
        return change;
    }

    private boolean removeFirstOperator(AqlQuery aqlQuery) {
        boolean change = false;
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        if (aqlElements.size() > 1) {
            AqlQueryElement aqlQueryElement = aqlElements.get(0);
            if (aqlQueryElement.isOperator()) {
                aqlElements.remove(0);
                change = true;
            }
        }
        return change;
    }

    private ItemTypeHandleEnum findItemTypeInstances(AqlQuery aqlQuery) {
        List<AqlQueryElement> aqlElements = aqlQuery.getAqlElements();
        boolean foundFileOrFolder = false;
        boolean foundAny = false;
        for (AqlQueryElement aqlQueryElement : aqlElements) {
            if (aqlQueryElement instanceof SimpleCriteria) {
                AqlField field = (AqlField) ((SimpleCriteria) aqlQueryElement).getVariable1();
                AqlValue value = (AqlValue) ((SimpleCriteria) aqlQueryElement).getVariable2();
                if (itemType == field.getFieldEnum() && (((Integer) value.toObject()) == AqlItemTypeEnum.file.type ||
                        ((Integer) value.toObject()) == AqlItemTypeEnum.folder.type)) {
                    foundFileOrFolder = true;
                }
                if (itemType == field.getFieldEnum() && ((Integer) value.toObject()) == AqlItemTypeEnum.any.type) {
                    foundAny = true;
                }
            }
        }
        return ItemTypeHandleEnum.fromFlags(foundFileOrFolder, foundAny);
    }

    private enum ItemTypeHandleEnum {
        none, replace, untuch, remove;

        public static ItemTypeHandleEnum fromFlags(boolean fileFolderFlag, boolean anyFlag) {
            if (fileFolderFlag && anyFlag) {
                return replace;
            } else if (fileFolderFlag) {
                return untuch;
            } else if (anyFlag) {
                return remove;
            } else {
                return none;
            }


        }
    }
}
