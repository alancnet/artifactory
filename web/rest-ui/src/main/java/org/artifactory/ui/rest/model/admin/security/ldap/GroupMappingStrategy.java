package org.artifactory.ui.rest.model.admin.security.ldap;

import org.artifactory.rest.common.model.BaseModel;

/**
 * @author Chen Keinan
 */
public class GroupMappingStrategy extends BaseModel {

    private String groupKeyMember;
    private String filter;
    private String groupNameAttribute;
    private String description;

    public GroupMappingStrategy(String groupKeyMember, String filter, String groupNameAttribute, String description) {
        this.groupKeyMember = groupKeyMember;
        this.filter = filter;
        this.groupNameAttribute = groupNameAttribute;
        this.description = description;
    }

    public String getGroupKeyMember() {
        return groupKeyMember;
    }

    public void setGroupKeyMember(String groupKeyMember) {
        this.groupKeyMember = groupKeyMember;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getGroupNameAttribute() {
        return groupNameAttribute;
    }

    public void setGroupNameAttribute(String groupNameAttribute) {
        this.groupNameAttribute = groupNameAttribute;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
