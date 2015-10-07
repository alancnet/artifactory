package org.artifactory.ui.rest.service.admin.security.general;

import org.artifactory.api.security.MasterEncryptionService;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
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
public class EncryptDecryptService implements RestService {

    @Autowired
    MasterEncryptionService masterEncryptionService;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        String id = request.getPathParamByKey("action");
        if (id == null || id.length() == 0) {
            response.responseCode(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        //encrypt / decrypt password
        encryptDecryptSecurityConfig(id,response);
    }

    /**
     * encrypt / decrypt security config by
     * @param id - encrypt or decrypt
     */
    private void encryptDecryptSecurityConfig(String id,RestResponse restResponse) {
        if (id.equals("encrypt")){
            masterEncryptionService.encrypt();
            restResponse.info("All passwords in your configuration are currently encrypted.");
        }
        else{
            masterEncryptionService.decrypt();
            restResponse.info("All passwords in your configuration are currently visible in plain text.");
        }
    }
}
