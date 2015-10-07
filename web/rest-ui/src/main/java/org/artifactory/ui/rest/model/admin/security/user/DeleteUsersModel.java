package org.artifactory.ui.rest.model.admin.security.user;

import com.google.common.collect.Lists;
import org.artifactory.rest.common.model.RestModel;

import java.util.List;

/**
 * @author Gidi Shabat
 *
 * Transfer Object that alloes to delete bulk of users
 */
public class DeleteUsersModel implements RestModel {
    private List<String> userNames = Lists.newArrayList();

    public List<String> getUserNames() {
        return userNames;
    }

    public void addUser(String userName) {
        userNames.add(userName);
    }
}
