package org.artifactory.aql.model;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DomainSensitiveField {
    private AqlFieldEnum field;
    private List<AqlDomainEnum> subDomains;

    public DomainSensitiveField(AqlFieldEnum field, List<AqlDomainEnum> domains) {
        this.field = field;
        subDomains = domains;
    }

    public AqlFieldEnum getField() {
        return field;
    }

    public void setField(AqlFieldEnum field) {
        this.field = field;
    }

    public List<AqlDomainEnum> getSubDomains() {
        return subDomains;
    }

    public void setSubDomains(List<AqlDomainEnum> subDomains) {
        this.subDomains = subDomains;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainSensitiveField that = (DomainSensitiveField) o;

        if (field != that.field) {
            return false;
        }
        if (subDomains != null ? !subDomains.equals(that.subDomains) : that.subDomains != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = field != null ? field.hashCode() : 0;
        result = 31 * result + (subDomains != null ? subDomains.hashCode() : 0);
        return result;
    }
}
