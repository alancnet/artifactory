package org.artifactory.ui.rest.model.admin.advanced.systemlogs;

import org.artifactory.common.ConstantValues;
import org.artifactory.rest.common.model.BaseModel;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Lior Hasson
 */
public class SystemLogsInitialize extends BaseModel {
    private final List<String> logs = asList("artifactory.log", "access.log", "import.export.log", "request.log");

    private int refreshRateSecs = ConstantValues.logsViewRefreshRateSecs.getInt();

    public List<String> getLogs() {
        return logs;
    }

    public int getRefreshRateSecs() {
        return refreshRateSecs;
    }
}
