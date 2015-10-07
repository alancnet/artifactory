package org.artifactory.common.wicket.util;

import org.apache.wicket.Component;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

/**
 * @author Yoav Aharoni
 */
public class SetEnableVisitor implements IVisitor<Component, Void> {
    private boolean enabled;

    public SetEnableVisitor(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void component(Component component, IVisit<Void> visit) {
        component.setEnabled(enabled);
    }
}
