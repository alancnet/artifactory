package org.artifactory.ui.rest.model.builds;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.artifactory.rest.common.model.BaseModel;
import org.jfrog.build.api.Build;
import org.jfrog.build.api.release.PromotionStatus;

/**
 * @author Chen Keinan
 */
public class PromotionStatusModel extends BaseModel {


    public static final String RELEASED = "Released";

    private String repository;
    private String comment;
    private String ciUser;
    private String user;
    private String status;
    private String timestamp;

    public PromotionStatusModel(PromotionStatus promotionStatus) {
        setStatus(promotionStatus.getStatus());
        setCiUser(promotionStatus.getCiUser());
        setComment(promotionStatus.getComment());
        setRepository(promotionStatus.getRepository());
        setTimestamp(promotionStatus.getTimestamp());
        setUser(promotionStatus.getUser());
    }

    public PromotionStatusModel(String status, String comment, String repository, String timestamp, String user,
                                String ciUser) {
        this.status = status;
        this.comment = comment;
        this.repository = repository;
        this.timestamp = timestamp;
        this.user = user;
        this.ciUser = ciUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Date getTimestampDate() {
        if (timestamp == null) {
            throw new IllegalArgumentException("Cannot parse a null timestamp as a date");
        }
        SimpleDateFormat format = new SimpleDateFormat(Build.STARTED_FORMAT);
        try {
            return format.parse(timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCiUser() {
        return ciUser;
    }

    public void setCiUser(String ciUser) {
        this.ciUser = ciUser;
    }
}
