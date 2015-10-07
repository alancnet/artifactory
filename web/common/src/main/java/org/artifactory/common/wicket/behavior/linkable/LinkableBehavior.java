package org.artifactory.common.wicket.behavior.linkable;

import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.Model;
import org.artifactory.common.wicket.behavior.tooltip.TooltipBehavior;
import org.artifactory.common.wicket.contributor.ResourcePackage;

/**
 * @author Yoav Aharoni
 */
public class LinkableBehavior extends ResourcePackage {
    public LinkableBehavior() {
        super(LinkableBehavior.class);
        addJavaScript();
    }

    @Override
    public void bind(Component component) {
        component.add(new TooltipBehavior(new Model<>("[Ctrl] + Click to open this URL.")));
    }

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        tag.put("class", "linkable");
        tag.put("onclick", "return Linkable.onclick(this, event);");
        tag.put("onmouseover", "return Linkable.onmouseover(this, event);");
        tag.put("onkeydown", "return Linkable.onkeyevent(this, event);");
        tag.put("onkeyup", "return Linkable.onkeyevent(this, event);");
    }
}
