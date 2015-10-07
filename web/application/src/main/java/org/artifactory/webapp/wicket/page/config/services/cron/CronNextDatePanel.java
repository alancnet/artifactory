/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2012 JFrog Ltd.
 *
 * Artifactory is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Artifactory is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Artifactory.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.artifactory.webapp.wicket.page.config.services.cron;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;
import org.artifactory.common.wicket.behavior.JavascriptEvent;
import org.artifactory.common.wicket.component.links.BaseTitledLink;
import org.artifactory.webapp.wicket.util.CronUtils;

import java.util.Date;

/**
 * @author Yoav Aharoni
 */
public class CronNextDatePanel extends Panel {
    public CronNextDatePanel(String id, final FormComponent cronExpField) {
        super(id);

        String nextRun = getNextRunTime(cronExpField.getValue());
        final Label nextRunLabel = new Label("cronExpNextRun", nextRun);
        nextRunLabel.setOutputMarkupId(true);
        add(nextRunLabel);

        // send ajax request with only cronExpField
        BaseTitledLink calculateButton = new BaseTitledLink("calculate", "Refresh");
        calculateButton.add(new JavascriptEvent("onclick", new AbstractReadOnlyModel() {
            @Override
            public Object getObject() {
                return "dojo.byId('" + cronExpField.getMarkupId() + "').onchange();";
            }
        }));
        add(calculateButton);

        // update nextRunLabel on cronExpField change
        cronExpField.setOutputMarkupId(true);
        cronExpField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                nextRunLabel.setDefaultModelObject(getNextRunTime(cronExpField.getValue()));
                target.add(nextRunLabel);
            }

            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                super.onError(target, e);
                nextRunLabel.setDefaultModelObject(getNextRunTime(cronExpField.getValue()));
                target.add(nextRunLabel);
            }

            @Override
            protected IAjaxCallDecorator getAjaxCallDecorator() {
                return new NoAjaxIndicatorDecorator();
            }
        });
    }

    private String getNextRunTime(String cronExpression) {
        if (StringUtils.isEmpty(cronExpression)) {
            return "The cron expression is blank.";
        }
        if (CronUtils.isValid(cronExpression)) {
            Date nextExecution = CronUtils.getNextExecution(cronExpression);
            if (nextExecution != null) {
                return formatDate(nextExecution);
            } else {
                return "Next run time is in the past.";
            }
        }
        return "The cron expression is invalid.";
    }


    private String formatDate(Date nextRunDate) {
        return nextRunDate.toString();
    }


}
