package org.artifactory.ui.rest.model.admin.security.group;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 */
public class DeleteGroupsModel implements RestModel {
    private List<String> groupNames = Lists.newArrayList();

    public List<String> getGroupNames() {
        return groupNames;
    }

    public void addGroup(String groupName) {
        groupNames.add(groupName);
    }
}