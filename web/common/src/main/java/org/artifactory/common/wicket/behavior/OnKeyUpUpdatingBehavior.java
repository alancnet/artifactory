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

package org.artifactory.common.wicket.behavior;

import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.util.time.Duration;
import org.artifactory.common.wicket.ajax.NoAjaxIndicatorDecorator;

/**
 * An {@link org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior} that invoked on 'onkeyup' events. There is
 * a default throttling of 500 milliseconds.
 *
 * @author Yossi Shaul
 */
public abstract class OnKeyUpUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {

    public OnKeyUpUpdatingBehavior() {
        this(500);
    }

    public OnKeyUpUpdatingBehavior(long throttlingDelayMillis) {
        super("onkeyup");
        setThrottleDelay(Duration.milliseconds(throttlingDelayMillis));
    }

    @Override
    protected IAjaxCallDecorator getAjaxCallDecorator() {
        return new NoAjaxIndicatorDecorator();
    }
}
