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

package org.artifactory.common.wicket.contributor;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.css.ICssCompressor;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.JavaScriptResourceReference;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.string.interpolator.PropertyVariableInterpolator;
import org.apache.wicket.util.template.PackageTextTemplate;
import org.artifactory.common.wicket.util.JavaScriptUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Improved version of HeaderContributor: <ul> <li>Naming conventions for default file name.</li> <li>Supports
 * interpolated templates.</li> <li>May hold more than one resource.</li> </ul>
 *
 * @author Yoav Aharoni
 */
public class ResourcePackage extends Behavior {
    private List<IHeaderContributor> contributors = new ArrayList<>();
    private Class<?> scope;

    protected ResourcePackage() {
        scope = getClass();
    }

    public ResourcePackage(Class<?> scope) {
        this.scope = scope;
    }

    public ResourcePackage dependsOn(ResourcePackage resourcePackage) {
        contributors.addAll(0, resourcePackage.contributors);
        return this;
    }

    public ResourcePackage addCss() {
        return addCss(getDefaultCssPath());
    }

    public ResourcePackage addJavaScript() {
        return addJavaScript(getDefaultJavaScriptPath());
    }

    public ResourcePackage addCssTemplate() {
        return addCssTemplate(getDefaultCssPath());
    }

    public ResourcePackage addJavaScriptTemplate() {
        return addJavaScriptTemplate(getDefaultJavaScriptPath());
    }

    public ResourcePackage addCss(final String path) {
        return addCss(path, null);
    }

    public ResourcePackage addCss(final String path, final String media) {
        add(new IHeaderContributor() {
            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderCSSReference(new CssResourceReference(scope, path), media);
            }
        });
        return this;
    }

    public ResourcePackage addJavaScript(final String path) {
        add(new IHeaderContributor() {
            @Override
            public void renderHead(IHeaderResponse response) {
                response.renderJavaScriptReference(new JavaScriptResourceReference(scope, path));
            }
        });
        return this;
    }

    public ResourcePackage addCssTemplate(final String path) {
        add(new IHeaderContributor() {
            @Override
            public void renderHead(IHeaderResponse response) {
                String script = readInterpolatedString(path);
                ICssCompressor compressor = Application.get().getResourceSettings().getCssCompressor();
                if (compressor != null) {
                    script = compressor.compress(script);
                }
                response.renderCSS(script, null);
            }
        });
        return this;
    }

    public ResourcePackage addJavaScriptTemplate(final String path) {
        add(new IHeaderContributor() {
            @Override
            public void renderHead(IHeaderResponse response) {
                String script = readInterpolatedString(path);
                script = JavaScriptUtils.compress(script);
                response.renderJavaScript(script, null);
            }
        });
        return this;
    }

    public String getResourceURL(String path) {
        ResourceReference reference = new PackageResourceReference(scope, path);
        return RequestCycle.get().urlFor(reference, null).toString();
    }

    public static ResourcePackage forCss(Class scope) {
        return new ResourcePackage(scope).addCss();
    }

    public static ResourcePackage forJavaScript(Class scope) {
        return new ResourcePackage(scope).addJavaScript();
    }

    private String getDefaultJavaScriptPath() {
        return scope.getSimpleName() + ".js";
    }

    private String getDefaultCssPath() {
        return scope.getSimpleName() + ".css";
    }

    private String readInterpolatedString(String path) {
        try {
            PackageTextTemplate resource = new PackageTextTemplate(scope, path);
            return PropertyVariableInterpolator.interpolate(resource.getString(), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void add(IHeaderContributor contributor) {
        contributors.add(contributor);
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        for (IHeaderContributor contributor : contributors) {
            contributor.renderHead(response);
        }
    }
}
