package org.artifactory.common.wicket.behavior;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;

/**
 * @author Yoav Aharoni
 */
public class NopFormComponentUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
    public NopFormComponentUpdatingBehavior(String event) {
        super(event);
    }

    @Override
    protected void onUpdate(AjaxRequestTarget target) {
    }

    @Override
    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new NoAjaxIndicatorDecorator();
    }
}
