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

package org.artifactory.common.wicket.component.template;

import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;

import java.util.Map;

/**
 * @author Yoav Aharoni
 */
public class ModelVariableInterpolator extends VariableInterpolator {
    private Map<String, IModel> variables;

    public ModelVariableInterpolator(String string, Map<String, IModel> variables) {
        super(string);
        this.variables = variables;
    }

    /**
     * Constructor.
     *
     * @param string                  a <code>String</code> to interpolate into
     * @param variables               the variables to substitute
     * @param exceptionOnNullVarValue if <code>true</code> an {@link IllegalStateException} will be thrown if {@link
     *                                #getValue(String)} returns <code>null</code>, otherwise the
     *                                <code>${varname}</code> string will be left in the <code>String</code> so that
     *                                multiple interpolators can be chained
     */
    public ModelVariableInterpolator(String string, Map<String, IModel> variables, boolean exceptionOnNullVarValue) {
        super(string, exceptionOnNullVarValue);
        this.variables = variables;
    }

    public void setVariables(Map<String, IModel> variables) {
        this.variables = variables;
    }

    @Override
    protected final String getValue(String variableName) {
        IModel model = variables.get(variableName);
        if (model == null) {
            return null;
        }
        Object value = model.getObject();
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static String interpolate(String string, Map<String, IModel> variables) {
        return new ModelVariableInterpolator(string, variables).toString();
    }
}
