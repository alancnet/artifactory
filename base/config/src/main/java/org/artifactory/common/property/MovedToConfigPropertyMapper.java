package org.artifactory.common.property;

/**
 * @author Dan Feldman
 */
class MovedToConfigPropertyMapper extends PropertyMapperBase {

    protected MovedToConfigPropertyMapper(String origPropertyName) {
        super(origPropertyName);
    }

    @Override
    public String map(String origValue) {
        return null;
    }

}