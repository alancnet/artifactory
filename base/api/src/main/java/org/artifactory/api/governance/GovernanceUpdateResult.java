package org.artifactory.api.governance;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * @author mamo
 */
public class GovernanceUpdateResult {

    public enum UpdateStatus {
        create, deleteOrUpdate, error, skip;

        @Override
        public String toString() {
            return BlackDuckUtils.camelize(name());
        }
    }

    public final GovernanceRequestInfo requestInfo;
    public final UpdateStatus updateStatus;
    private String message;

    public GovernanceUpdateResult(GovernanceRequestInfo requestInfo, String updateStatus) {
        this.requestInfo = requestInfo;
        this.updateStatus = getUpdateStatus(updateStatus);
    }

    /**
     * get updated enum status
     *
     * @param updateStatus - updated status
     * @return - enum status
     */
    public UpdateStatus getUpdateStatus(String updateStatus) {
        switch (updateStatus) {
            case "create":
                return UpdateStatus.create;
            case "deleteOrUpdate":
                return UpdateStatus.deleteOrUpdate;
            case "error":
                return UpdateStatus.error;
            case "skip":
                return UpdateStatus.skip;
            default:
                return UpdateStatus.create;
        }
    }

    public GovernanceUpdateResult message(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public static int numberOfErrors(List<GovernanceUpdateResult> resultList) {
        if (CollectionUtils.isNotEmpty(resultList)) {
            ImmutableList<GovernanceUpdateResult> errors = Multimaps.index(resultList, $updateStatus).get(
                    UpdateStatus.error);
            return errors != null ? errors.size() : 0;
        } else {
            return 0;
        }
    }

    public static final Function<GovernanceUpdateResult, UpdateStatus> $updateStatus = new Function<GovernanceUpdateResult, UpdateStatus>() {
        @Override
        public UpdateStatus apply(GovernanceUpdateResult input) {
            return input.updateStatus;
        }
    };

}
