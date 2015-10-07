package org.artifactory.ui.rest.service.admin.advanced.systemlogs;

import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.rest.common.service.StreamRestResponse;
import org.artifactory.ui.rest.model.admin.advanced.systemlogs.SystemLogFile;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSysLogDownloadLinkService implements RestService {
    private File logDir = ContextHelper.get().getArtifactoryHome().getLogDir();

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        updateResponseWithLogFile(request, response);
    }

    /**
     * update response with the log data file
     *
     * @param artifactoryRequest  - encapsulate data related to the request
     * @param artifactoryResponse  - encapsulate data related to the response
     */
    private void updateResponseWithLogFile(ArtifactoryRestRequest artifactoryRequest,
                                           RestResponse artifactoryResponse) {

        String selectedLog = artifactoryRequest.getQueryParamByKey("id");
        File systemLogFile = new File(logDir, selectedLog);

        if (systemLogFile.exists()) {
            ((StreamRestResponse) artifactoryResponse).setDownloadFile(systemLogFile.getName());
            ((StreamRestResponse) artifactoryResponse).setDownload(true);
            SystemLogFile logFileModel = new SystemLogFile();

            try {
                FileInputStream stream = new FileInputStream(systemLogFile);
                logFileModel.setStream(stream);
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage());
            }

            artifactoryResponse.iModel(logFileModel);
        }
    }
}
