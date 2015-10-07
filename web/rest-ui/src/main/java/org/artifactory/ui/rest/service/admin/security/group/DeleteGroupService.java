package org.artifactory.ui.rest.service.admin.security.group;

import org.artifactory.api.security.UserGroupService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.security.group.DeleteGroupsModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;


/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteGroupService<T extends DeleteGroupsModel> implements RestService<T> {
    @Autowired
    protected UserGroupService userGroupService;

    @Override
    public void execute(ArtifactoryRestRequest<T> request, RestResponse response) {
        T model = request.getImodel();
        for (String groupName : model.getGroupNames()) {
            if (groupName == null || groupName.length() == 0) {
                response.responseCode(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            userGroupService.deleteGroup(groupName);

        }
        if(model.getGroupNames().size()>1){
            response.info("Successfully removed "+model.getGroupNames().size()+" groups");
        }else if(model.getGroupNames().size()==1){
            response.info("Successfully removed group '" + model.getGroupNames().get(0) + "'");
        }
    }
}
