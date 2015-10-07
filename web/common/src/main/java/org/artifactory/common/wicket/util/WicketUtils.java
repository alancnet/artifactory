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

package org.artifactory.common.wicket.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Closeables;
import org.apache.commons.compress.utils.CharsetNames;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.RequestUtils;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Packages;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.artifactory.common.ConstantValues;
import org.artifactory.common.wicket.application.ResponsePageSupport;
import org.artifactory.common.wicket.behavior.CssClass;
import org.artifactory.common.wicket.component.TextContentPanel;
import org.artifactory.common.wicket.component.label.highlighter.Syntax;
import org.artifactory.common.wicket.component.label.highlighter.SyntaxHighlighter;
import org.artifactory.util.HttpUtils;
import org.artifactory.util.Pair;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author yoavl
 */
public abstract class WicketUtils {
    private static LoadingCache<Pair<Class, String>, String> resourceCache = createResourceCache();

    private WicketUtils() {
        // utility class
    }

    private static LoadingCache<Pair<Class, String>, String> createResourceCache() {
        String dev = System.getProperty(ConstantValues.dev.getPropertyName());
        if (StringUtils.isBlank(dev)) {
            dev = System.getProperty("aol.devMode");
        }
        boolean isDev = Boolean.parseBoolean(dev);
        return CacheBuilder.newBuilder().
                softValues().
                maximumSize(100).
                expireAfterWrite(isDev ? 30 : 0, TimeUnit.SECONDS).
                build(new CacheLoader<Pair<Class, String>, String>() {
                    @Override
                    public String load(Pair<Class, String> pair) throws Exception {
                        return readResourceNoCache(pair.getFirst(), pair.getSecond());
                    }
                });
    }

    /**
     * Get the absolute bookmarkable path of a page
     *
     * @param pageClass Page
     * @return Bookmarkable path
     */
    public static String absoluteMountPathForPage(Class<? extends Page> pageClass) {
        return absoluteMountPathForPage(pageClass, new PageParameters());
    }

    /**
     * Get the absolute bookmarkable path of a page
     *
     * @param pageClass      Page
     * @param pageParameters Optional page parameters
     * @return Bookmarkable path
     */
    public static String absoluteMountPathForPage(Class<? extends Page> pageClass, PageParameters pageParameters) {
        HttpServletRequest req = getHttpServletRequest();
        RequestCycle requestCycle = RequestCycle.get();
        Url url = requestCycle.mapUrlFor(pageClass, pageParameters);
        String renderedUrl = url.toString();
        renderedUrl = Strings.isEmpty(renderedUrl) ? "." : renderedUrl;
        return RequestUtils.toAbsolutePath(HttpUtils.getWebappContextUrl(req),
                requestCycle.getOriginalResponse().encodeURL(renderedUrl));
    }

    public static WebRequest getWebRequest() {
        RequestCycle requestCycle = RequestCycle.get();
        if (requestCycle == null) {
            return null;
        }
        return (WebRequest) requestCycle.getRequest();
    }

    public static WebResponse getWebResponse() {
        return (WebResponse) RequestCycle.get().getResponse();
    }

    public static Map<String, String> getHeadersMap() {
        Map<String, String> map = new HashMap<>();
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = (String) headerNames.nextElement();
                map.put(headerName.toUpperCase(), request.getHeader(headerName));
            }
        }
        return map;
    }

    public static HttpServletRequest getHttpServletRequest() {
        return (HttpServletRequest) WicketUtils.getWebRequest().getContainerRequest();
    }

    @Nullable
    public static Page getPage() {
        return ResponsePageSupport.getResponsePage();
    }

    public static String getWicketAppPath() {
        Request request = RequestCycle.get().getRequest();
        return request.getContextPath() + request.getFilterPath() + "/";
    }

    public static String readResource(Class scope, String file) {
        try {
            return resourceCache.get(new Pair<>(scope, file));
        } catch (ExecutionException e) {
            throw new RuntimeException("Could not read resource " + file);
        }
    }

    private static String readResourceNoCache(Class scope, String file) {
        InputStream inputStream = null;
        try {
            final String path = Packages.absolutePath(scope, file);
            final IResourceStream resourceStream = Application.get().getResourceSettings().getResourceStreamLocator()
                    .locate(scope, path);
            inputStream = resourceStream.getInputStream();
            return Streams.readString(inputStream, "utf-8");
        } catch (ResourceStreamNotFoundException e) {
            throw new RuntimeException(String.format("Can't find resource \"%s.%s\"", scope.getName(), file), e);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Can't read resource \"%s.%s\"", scope.getName(), file), e);
        } finally {
            Closeables.closeQuietly(inputStream);
        }
    }

    /**
     * Returns a syntax highlighter. If the size of the string exceeds the size limit defined in the system properties,
     * than a simple text content panel will be returned
     *
     * @param componentId ID to assign to the returned component
     * @param toDisplay   String to display
     * @param syntaxType  Type of syntax to use
     * @return Text displaying component
     */
    public static Component getSyntaxHighlighter(String componentId, String toDisplay, Syntax syntaxType) {
        try {
            if (toDisplay != null && ConstantValues.uiSyntaxColoringMaxTextSizeBytes.getLong() >=
                    toDisplay.getBytes(CharsetNames.UTF_8).length) {
                return new SyntaxHighlighter(componentId, toDisplay, syntaxType);
            } else {
                TextContentPanel contentPanel = new TextContentPanel(componentId);
                contentPanel.add(new CssClass("lines"));
                return contentPanel.setContent(toDisplay);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toAbsolutePath(final String relativePagePath) {
        HttpServletRequest req = getHttpServletRequest();
        return RequestUtils.toAbsolutePath(req.getRequestURL().toString(), relativePagePath);
    }

    public static String getParameter(String confirm) {
        return RequestCycle.get().getRequest().getRequestParameters().getParameterValue(confirm).toString();
    }

    public static HttpServletResponse getHttpServletResponse() {
        return (HttpServletResponse) getWebResponse().getContainerResponse();
    }
}