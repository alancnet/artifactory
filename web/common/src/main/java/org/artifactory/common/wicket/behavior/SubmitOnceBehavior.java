package org.artifactory.common.wicket.behavior;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.Model;

/**
 * @author Yoav Aharoni
 */
public class SubmitOnceBehavior extends AttributeAppender {
    public SubmitOnceBehavior() {
        super("onsubmit", Model.of("DomUtils.submitOnce(this);"), ";");
    }
}
