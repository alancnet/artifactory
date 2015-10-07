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

package org.artifactory.webapp.wicket.util;

import org.apache.commons.lang.StringUtils;
import org.artifactory.descriptor.repo.VirtualRepoDescriptor;
import org.artifactory.util.XmlUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.artifactory.common.wicket.util.JavaScriptUtils.jsParam;

/**
 * @author Yoav Aharoni
 */
public abstract class JnlpUtils {
    private static final Logger log = LoggerFactory.getLogger(JnlpUtils.class);

    private JnlpUtils() {
        // utility class
    }

    public static List<VirtualRepoDescriptor> filterNonWebstartRepos(
            Iterable<VirtualRepoDescriptor> virtualRepoContainingMe) {
        List<VirtualRepoDescriptor> webstartRepos = new ArrayList<>();
        for (VirtualRepoDescriptor descriptor : virtualRepoContainingMe) {
            if (StringUtils.isNotBlank(descriptor.getKeyPair())) {
                webstartRepos.add(descriptor);
            }
        }
        return webstartRepos;
    }

    public static boolean isJavaFxApplet(String jnlpContent) {
        try {
            Document document = XmlUtils.parse(jnlpContent);
            Element root = document.getRootElement();
            Element appletDesc = root.getChild("applet-desc");
            if (appletDesc != null) {
                String appletMainClass = appletDesc.getAttributeValue("main-class");
                return "com.sun.javafx.runtime.adapter.Applet".equals(appletMainClass);
            }
        } catch (Exception e) {
            log.warn("Failed to parse jnlp '{}': ", e.getMessage());
        }
        return false;
    }

    public static AppletInfo getAppletInfo(String jnlpContent, String jnlpHref) {
        if (isJavaFxApplet(jnlpContent)) {
            return new AppletInfo(jnlpContent, jnlpHref);
        }
        return null;
    }

    private static Element getMainJavaFXScript(Element appletDesc) {
        for (Object param : appletDesc.getChildren("param")) {
            Element paramElement = (Element) param;
            String paramName = paramElement.getAttributeValue("name");
            if ("MainJavaFXScript".equals(paramName)) {
                return paramElement;
            }
        }

        // not found
        return null;
    }

    public static class AppletInfo implements Serializable {
        private final String appletName;
        private final String mainClass;
        private final int width;
        private final int height;
        private String jnlpHref;
        private String jarHref;
        private String mainJnlpHref;
        private final String scriptSnippet;

        private AppletInfo(String jnlpContent, String jnlpHref) {
            Document document = XmlUtils.parse(jnlpContent);
            Element root = document.getRootElement();
            Element appletDesc = root.getChild("applet-desc");
            appletName = appletDesc.getAttributeValue("name");
            Element mainClassParam = getMainJavaFXScript(appletDesc);
            if (mainClassParam == null) {
                log.warn("Main java fx script not found");
                mainClass = "NOT_FOUND";
            } else {
                mainClass = mainClassParam.getAttributeValue("value");
            }
            width = Integer.parseInt(appletDesc.getAttributeValue("width"));
            height = Integer.parseInt(appletDesc.getAttributeValue("height"));

            this.jnlpHref = jnlpHref;
            jarHref = jnlpHref.replace("-browser.jnlp", ".jar");
            mainJnlpHref = jnlpHref.replace("-browser.jnlp", ".jnlp");

            scriptSnippet = generateScriptSnippet();
        }

        public String getAppletName() {
            return appletName;
        }

        public String getMainClass() {
            return mainClass;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getJnlpHref() {
            return jnlpHref;
        }

        public String getJarHref() {
            return jarHref;
        }

        public String getMainJnlpHref() {
            return mainJnlpHref;
        }

        public String getScriptSnippet() {
            return scriptSnippet;
        }

        public String generateScriptSnippet() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n<script src=\"http://dl.javafx.com/1.2/dtfx.js\"></script>\n")
                    .append("<script type=\"text/javascript\">\n").append("var u10only = '';\n")
                    .append("var webstartAllowed = '';\n").append("\n")
                    .append("if(u10only=='true' && !deployJava.isPlugin2()) {\n")
                    .append("    document.write(\"<p class='u10warning'>\");\n")
                    .append("    document.write(\"This sample needs Java SE 6 Update 10 or higher with Internet Explorer 7+ or FireFox 3+\");\n")
                    .append("    document.write(\"\");\n")
                    .append("    if(webstartAllowed=='true') {\n")
                    .append("        document.write(\" Please <a href='").append(mainJnlpHref)
                    .append("'>run this example with Java Webstart<a> instead\");\n")
                    .append("    }\n")
                    .append("    document.write(\"</p>\");\n")
                    .append("} else {\n")
                    .append("  javafx(\n")
                    .append("    {\n")
                    .append(format("      archive: %s,%n", jsParam(jarHref)))
                    .append(format("      jnlp_href: %s,%n", jsParam(jnlpHref)))
                    .append("      draggable: true,\n")
                    .append(format("      width: %s,%n", getWidth()))
                    .append(format("      height: %s,%n", getHeight()))
                    .append(format("      code: %s,%n", jsParam(getMainClass())))
                    .append(format("      name: %s%n", jsParam(getAppletName())))
                    .append("    }\n")
                    .append("  );\n")
                    .append("}\n")
                    .append("</script>");

            return sb.toString();
        }
    }
}
