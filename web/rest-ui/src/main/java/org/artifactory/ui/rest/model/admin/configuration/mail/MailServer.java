package org.artifactory.ui.rest.model.admin.configuration.mail;

import org.artifactory.descriptor.mail.MailServerDescriptor;
import org.artifactory.rest.common.model.RestModel;
import org.artifactory.rest.common.util.JsonUtil;

/**
 * @author Chen Keinan
 */
public class MailServer extends MailServerDescriptor implements RestModel {

    private String testReceipt;

    public String getTestReceipt() {
        return testReceipt;
    }

    public void setTestReceipt(String testReceipt) {
        this.testReceipt = testReceipt;
    }

    public MailServer() {
        super.setEnabled(false);
    }

    public MailServer(MailServerDescriptor mailServerDescriptor) {
        super.setEnabled(mailServerDescriptor.isEnabled());
        super.setHost(mailServerDescriptor.getHost());
        super.setPassword(mailServerDescriptor.getPassword());
        super.setUsername(mailServerDescriptor.getUsername());
        super.setArtifactoryUrl(mailServerDescriptor.getArtifactoryUrl());
        super.setFrom(mailServerDescriptor.getFrom());
        super.setPort(mailServerDescriptor.getPort());
        super.setTls(mailServerDescriptor.isTls());
        super.setSsl(mailServerDescriptor.isSsl());
        super.setSubjectPrefix(mailServerDescriptor.getSubjectPrefix());
    }

    @Override
    public String toString() {
        return JsonUtil.jsonToString(this);
    }
}
