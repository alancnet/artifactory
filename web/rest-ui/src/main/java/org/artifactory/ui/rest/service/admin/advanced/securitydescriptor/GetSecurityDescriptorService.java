package org.artifactory.ui.rest.service.admin.advanced.securitydescriptor;

import com.thoughtworks.xstream.XStream;
import org.artifactory.api.security.SecurityService;
import org.artifactory.factory.InfoFactoryHolder;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.SecurityInfo;
import org.artifactory.ui.rest.model.admin.advanced.securitydescriptor.SecurityDescriptorModel;
import org.artifactory.ui.rest.service.utils.AolUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSecurityDescriptorService implements RestService {

    @Autowired
    private SecurityService securityService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        AolUtils.assertNotAol("GetSecurityDescriptor");
        // get security descriptor model
        SecurityDescriptorModel securityDescriptorModel = getSecurityDescriptorModel();
        // update response data
        response.iModel(securityDescriptorModel);
    }

    /**
     * get security descriptor model from security descriptor info
     *
     * @return security descriptor model
     */
    private SecurityDescriptorModel getSecurityDescriptorModel() {
        SecurityInfo securityData = securityService.getSecurityData();
        XStream xstream = InfoFactoryHolder.get().getSecurityXStream();
        return new SecurityDescriptorModel(xstream.toXML(securityData));
    }
}
