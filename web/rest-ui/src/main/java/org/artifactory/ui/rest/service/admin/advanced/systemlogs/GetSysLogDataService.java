package org.artifactory.ui.rest.service.admin.advanced.systemlogs;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.context.ContextHelper;
import org.artifactory.rest.common.service.ArtifactoryRestRequest;
import org.artifactory.rest.common.service.RestResponse;
import org.artifactory.rest.common.service.RestService;
import org.artifactory.ui.rest.model.admin.advanced.systemlogs.SystemLogData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * @author Lior Hasson
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class GetSysLogDataService implements RestService {
    private static final int FIRST_READ_BLOCK_SIZE = 100 * 1024;
    private File logDir = ContextHelper.get().getArtifactoryHome().getLogDir();
    /**
     * Pointer to indicate the last position (in bytes) the log file was read from and showed in the page
     */
    private long lastPointer = 0;

    @Override
    public void execute(ArtifactoryRestRequest request, RestResponse response) {
        SystemLogData sys = new SystemLogData();
        String selectedLog = request.getQueryParamByKey("id");
        // Check log file exists. Protect against LFI security RTFACT-8215
        File[] logFiles = logDir.listFiles();
        File systemLogFile = null;
        for (File logFile : logFiles) {
            if (logFile.getName().equalsIgnoreCase(selectedLog)) {
                systemLogFile = logFile;
                break;
            }
        }
        if (systemLogFile == null) {
            response.responseCode(HttpStatus.SC_NOT_FOUND).error("Log named " + selectedLog + " not found!");
            return;
        }
        readLogFile(request, systemLogFile, sys);
        setLogInfo(systemLogFile, sys);

        response.iModel(sys);
    }

    private void readLogFile(ArtifactoryRestRequest artifactoryRequest, File systemLogFile, SystemLogData sys) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(artifactoryRequest.getQueryParamByKey("fileSize"))) {
            lastPointer = Long.parseLong(artifactoryRequest.getQueryParamByKey("fileSize"));
        }

        long fileSize = systemLogFile.length();

        //The file didn't changed, return zero to the client
        if (!systemLogFile.exists() || fileSize == lastPointer) {
            sys.setLogContent(StringUtils.EMPTY);
            lastPointer = 0;
            return;
        }

        try (RandomAccessFile logRandomAccessFile = new RandomAccessFile(systemLogFile, "r")) {
            String line;
            //If the log file is larger than 100K
            if (logRandomAccessFile.length() > FIRST_READ_BLOCK_SIZE) {
                //Point to the beginning of the last 100K
                lastPointer = logRandomAccessFile.length() - FIRST_READ_BLOCK_SIZE;
            } else {
                lastPointer = 0;
            }

            logRandomAccessFile.seek(lastPointer);
            while ((line = logRandomAccessFile.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            lastPointer = logRandomAccessFile.getFilePointer();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        sys.setLogContent(sb.toString());
    }

    private void setLogInfo(File systemLogFile, SystemLogData sys) {
        Date logLastModified = new Date(systemLogFile.lastModified());
        Date viewLastUpdate = new Date(System.currentTimeMillis());
        sys.setLastUpdateLabel(viewLastUpdate);
        sys.setLastUpdateModified(logLastModified);
        sys.setFileSize(lastPointer);
    }
}
