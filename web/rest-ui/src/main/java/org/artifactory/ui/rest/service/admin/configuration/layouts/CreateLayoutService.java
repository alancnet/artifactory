package org.artifactory.ui.rest.service.admin.configuration.layouts;

import org.artifactory.api.config.CentralConfigService;
import org.artifactory.descriptor.config.MutableCentralConfigDescriptor;
import org.artifactory.descriptor.repo.RepoLayout;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.service.admin.configuration.layouts.validation.LayoutFieldRequiredTokenValidator;
import org.artifactory.ui.rest.service.admin.configuration.layouts.validation.ReservedLayoutNameValidator;
import org.artifactory.ui.rest.service.utils.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lior Hasson
 */
@Component
public class CreateLayoutService implements RestService<RepoLayout> {
    private static final Logger log = LoggerFactory.getLogger(CreateLayoutService.class);
    @Autowired
    private CentralConfigService centralConfigService;

    @Override
    public void execute(ArtifactoryRestRequest<RepoLayout> request, RestResponse response) {
        RepoLayout repoLayout = request.getImodel();
        try {
            validation(repoLayout);

            MutableCentralConfigDescriptor mutableDescriptor = getMutableDescriptor();
            mutableDescriptor.addRepoLayout(repoLayout);
            centralConfigService.saveEditedDescriptorAndReload(mutableDescriptor);

            String message = "Successfully created layout '" + repoLayout.getName() + "'";
            response.info(message);
        } catch (ValidationException e) {
            response.error(e.getMessage());
            log.debug(e.getMessage());
        }
    }

    /**
     * @see LayoutFieldRequiredTokenValidator
     * @see ReservedLayoutNameValidator
     */
    private void validation(RepoLayout repoLayout) throws ValidationException {
            ReservedLayoutNameValidator.onValidate(repoLayout.getName());
            LayoutFieldRequiredTokenValidator.onValidate(repoLayout.getArtifactPathPattern());
            LayoutFieldRequiredTokenValidator.onValidate(repoLayout.getDescriptorPathPattern());
    }

    private MutableCentralConfigDescriptor getMutableDescriptor() {
        return centralConfigService.getMutableDescriptor();
    }
}
