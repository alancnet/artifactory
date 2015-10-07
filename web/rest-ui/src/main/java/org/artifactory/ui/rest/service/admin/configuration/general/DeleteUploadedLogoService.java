package org.artifactory.ui.rest.service.admin.configuration.general;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author chen keinan
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DeleteUploadedLogoService implements RestService {
    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        try {
            String logoDir = ContextHelper.get().getArtifactoryHome().getLogoDir().getAbsolutePath();
            File file = new File(logoDir, "logo");
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            response.error("error with deleting logo");
        }
    }
}
