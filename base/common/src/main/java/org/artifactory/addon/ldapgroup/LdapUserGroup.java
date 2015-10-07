package org.artifactory.addon.ldapgroup;

/**
 * @author Chen Keinan
 */
public class LdapUserGroup {
    private String groupName;
    private String description;
    private String groupDn;
    private Status requiredUpdate = Status.DOES_NOT_EXIST;

    protected LdapUserGroup() {
    }

    public LdapUserGroup(String groupName, String description, String groupDn) {
        this.groupName = groupName;
        this.description = description;
        this.groupDn = groupDn;
    }

    public Status getRequiredUpdate() {
        return requiredUpdate;
    }

    public void setRequiredUpdate(Status requiredUpdate) {
        this.requiredUpdate = requiredUpdate;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return description;
    }

    public String getGroupDn() {
        return groupDn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LdapUserGroup group = (LdapUserGroup) o;
        if (!groupName.equals(group.groupName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return groupName.hashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("LdapGroup");
        sb.append("{groupName='").append(groupName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public enum Status {
        DOES_NOT_EXIST("Ready to be imported."),
        REQUIRES_UPDATE("Group information is ou-of-date. DN has changed in LDAP."),
        IN_ARTIFACTORY("Group information is up-to-date in Artifactory.");

        private String description;

        Status(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
