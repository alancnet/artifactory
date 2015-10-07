package org.artifactory.ui.rest.service.admin.security.general;

import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.security.crypto.CryptoHelper;
import org.artifactory.ui.rest.model.admin.security.general.MasterKey;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * @author Chen Keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetMasterKeyService implements RestService {

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        MasterKey masterKey = new MasterKey();
        masterKey.setHasMasterKey(CryptoHelper.hasMasterKey());
        response.iModel(masterKey);
    }
}
