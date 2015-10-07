/*
 * Artifactory is a binaries repository manager.
 * Copyright (C) 2014 JFrog Ltd.
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

package org.artifactory.repo.webdav.methods;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.artifactory.api.repo.BaseBrowsableItem;
import org.artifactory.api.repo.BrowsableItem;
import org.artifactory.api.repo.BrowsableItemCriteria;
import org.artifactory.api.repo.RepositoryBrowsingService;
import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.mime.MimeType;
import org.artifactory.mime.NamingUtils;
import org.artifactory.repo.RepoPath;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.WebdavStatus;
import org.artifactory.repo.webdav.XmlWriter;
import org.artifactory.request.ArtifactoryRequest;
import org.artifactory.util.HttpUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.artifactory.repo.webdav.WebdavServiceImpl.*;

@Component
public class PropfindMethod extends AbstractWebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(PropfindMethod.class);

    @Autowired
    private InternalRepositoryService repoService;

    @Autowired
    private RepositoryBrowsingService repoBrowsing;

    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final DateTimeFormatter CREATION_DATE_FORMAT =
            DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(DateTimeZone.forID("GMT"))
                    .withLocale(Locale.ENGLISH);

    /**
     * RFC 1123 date format to use in "getlastmodified" attribute
     * http://tools.ietf.org/html/rfc2068#section-3.3.1
     */
    protected static final DateTimeFormatter LAST_MODIFIED_DATE_FORMAT =
            DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZone(DateTimeZone.forID("GMT"))
                    .withLocale(Locale.ENGLISH);

    /**
     * PROPFIND options
     */
    private enum PropfindType {
        FIND_ALL_PROP("allprop"),
        FIND_BY_PROPERTY("prop"),
        FIND_PROPERTY_NAMES("propname");
        private final String detectString;

        PropfindType(String detectString) {
            this.detectString = detectString;
        }

        @Nullable
        public static PropfindType fromString(String s) {
            for (PropfindType type : PropfindType.values()) {
                if (s.endsWith(type.detectString)) {
                    return type;
                }
            }
            return null;
        }
    }

    @Override
    public boolean canHandle(String method) {
        return getName().equalsIgnoreCase(method);
    }

    @Override
    public void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        // Retrieve the resources
        log.debug("Handling {}", getName());
        int depth = findDepth(request);
        List<String> properties = null;
        PropfindType propertyFindType = PropfindType.FIND_ALL_PROP;
        Node propNode = null;
        //get propertyNode and type
        if (request.getContentLength() > 0) {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            try {
                Document document = documentBuilder.parse(
                        new InputSource(request.getInputStream()));
                logWebdavRequest(document);
                // Get the root element of the document
                Element rootElement = document.getDocumentElement();
                NodeList childList = rootElement.getChildNodes();

                for (int i = 0; i < childList.getLength(); i++) {
                    Node currentNode = childList.item(i);
                    switch (currentNode.getNodeType()) {
                        case Node.TEXT_NODE:
                            break;
                        case Node.ELEMENT_NODE:
                            propertyFindType = PropfindType.fromString(currentNode.getNodeName());
                            if (propertyFindType == PropfindType.FIND_BY_PROPERTY) {
                                propNode = currentNode;
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Webdav propfind failed.", e);
            }
        }

        if (propertyFindType == PropfindType.FIND_BY_PROPERTY) {
            properties = getPropertiesFromXml(propNode);
        }

        response.setStatus(HttpStatus.SC_MULTI_STATUS);
        response.setContentType("text/xml; charset=UTF-8");

        // Create multistatus object
        Writer writer = response.getWriter();
        if (log.isDebugEnabled()) {
            writer = new StringWriter(); // write to memory so we'll be able to log the result as string
        }
        XmlWriter generatedXml = new XmlWriter(writer);
        generatedXml.writeXMLHeader();
        generatedXml.writeElement(DEFAULT_NS_ABBRV, "multistatus", XmlWriter.OPENING,
                DEFAULT_NS_ABBRV, DEFAULT_NAMESPACE, "ns0",
                DEFAULT_NAMESPACE);

        RepoPath repoPath = request.getRepoPath();
        BrowsableItem rootItem = null;
        if (repoService.exists(repoPath)) {
            rootItem = repoBrowsing.getLocalRepoBrowsableItem(repoPath);
        }
        if (rootItem != null) {
            recursiveParseProperties(request, generatedXml, rootItem, propertyFindType, properties,
                    depth);
        } else {
            log.warn("Item '" + request.getRepoPath() + "' not found.");
        }
        generatedXml.writeElement(DEFAULT_NS_ABBRV, "multistatus", XmlWriter.CLOSING);
        generatedXml.sendData();
        if (log.isDebugEnabled()) {
            log.debug("Webdav response:\n" + writer.toString());
            //response.setContentLength(writer.toString().getBytes().length);
            response.getWriter().append(writer.toString());
        }
        response.flush();
    }

    @Override
    public String getName() {
        return "propfind";
    }

    @SuppressWarnings({"OverlyComplexMethod"})
    private void parseProperties(ArtifactoryRequest request, XmlWriter xmlResponse,
            BaseBrowsableItem item, PropfindType type, List<String> propertiesList) throws IOException {
        // Last modified should be returned as RFC 1123 format.
        // See https://tools.ietf.org/html/rfc4918#section-8
        RepoPath repoPath = item.getRepoPath();
        String creationDate = CREATION_DATE_FORMAT.print(item.getCreated());
        boolean isFolder = item.isFolder();
        String rfcDate = LAST_MODIFIED_DATE_FORMAT.print(item.getLastModified());
        String resourceLength = item.getSize() + "";

        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "response", XmlWriter.OPENING, PROP_NS_ABBRV2, APACHE_NAMESPACE,
                PROP_NS_ABBRV1, DEFAULT_NAMESPACE);

        //Generating href element
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "href", XmlWriter.OPENING);
        String origPath = request.getPath();
        String uri = request.getUri();
        String hrefBase = uri;
        origPath = HttpUtils.encodeQuery(origPath);
        if (origPath.length() > 0) {
            int idx = uri.lastIndexOf(origPath);
            if (idx > 0) {
                //When called recursively avoid concatenating the original path on top of itself
                hrefBase = uri.substring(0, idx);
            }
        }
        String path = repoPath.getPath();
        if (StringUtils.isNotBlank(path) && !hrefBase.endsWith("/")) {
            hrefBase += "/";
        }

        // Encode only the path since the base is already encoded
        String href = hrefBase + HttpUtils.encodeQuery(path);

        xmlResponse.writeText(href);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "href", XmlWriter.CLOSING);

        String resourceName = path;
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash != -1) {
            resourceName = resourceName.substring(lastSlash + 1);
        }

        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " +
                WebdavStatus.getStatusText(WebdavStatus.SC_OK);
        switch (type) {
            case FIND_ALL_PROP:
                handleFindAllPropsResponse(xmlResponse, creationDate, isFolder, rfcDate, resourceLength, status,
                        path, resourceName);
                break;
            case FIND_PROPERTY_NAMES:
                handleFindPropNamesResponse(xmlResponse, isFolder, status);
                break;
            case FIND_BY_PROPERTY:
                handleFindByPropResponse(xmlResponse, propertiesList, creationDate, isFolder, rfcDate,
                        resourceLength, status, path, resourceName);
                break;

        }
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "response", XmlWriter.CLOSING);
    }

    private void handleFindByPropResponse(XmlWriter xmlResponse, List<String> propertiesList, String creationDate,
            boolean isFolder, String lastModified, String resourceLength, String status, String path,
            String resourceName) throws IOException {
        //noinspection MismatchedQueryAndUpdateOfCollection
        List<String> propertiesNotFound = new ArrayList<>();
        // Parse the list of properties
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.OPENING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.OPENING);
        for (String property : propertiesList) {
            switch (property) {
                case "creationdate":
                    xmlResponse.writeProperty(PROP_NS_ABBRV1, "creationdate", creationDate);
                    break;
                case "displayname":
                    xmlResponse.writeElement(DEFAULT_NS_ABBRV, "displayname", XmlWriter.OPENING);
                    xmlResponse.writeData(resourceName);
                    xmlResponse.writeElement(DEFAULT_NS_ABBRV, "displayname", XmlWriter.CLOSING);
                    break;
                case "getcontentlanguage":
                    if (isFolder) {
                        propertiesNotFound.add(property);
                    } else {
                        xmlResponse.writeElement(
                                null, "getcontentlanguage", XmlWriter.NO_CONTENT);
                    }
                    break;
                case "getcontentlength":
                    if (isFolder) {
                        propertiesNotFound.add(property);
                    } else {
                        xmlResponse.writeProperty(PROP_NS_ABBRV1, "getcontentlength", resourceLength);
                    }
                    break;
                case "getcontenttype":
                    if (isFolder) {
                        propertiesNotFound.add(property);
                    } else {
                        xmlResponse.writeProperty(DEFAULT_NS_ABBRV, "getcontenttype",
                                NamingUtils.getMimeTypeByPathAsString(path));
                    }
                    break;
                case "getetag":
                    xmlResponse.writeProperty(PROP_NS_ABBRV1, "getetag",
                            getEtag(resourceLength, lastModified));
                    break;
                case "getlastmodified":
                    xmlResponse.writeProperty(PROP_NS_ABBRV1, "getlastmodified", lastModified);
                    break;
                case "source":
                    xmlResponse.writeProperty(DEFAULT_NS_ABBRV, "source", "");
                    break;
                default:
                    propertiesNotFound.add(property);
                    break;
            }
        }
        //Always include resource type
        if (isFolder) {
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.OPENING);
            xmlResponse.writeElement(DEFAULT_NS_ABBRV, "collection", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.CLOSING);
        } else {
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.NO_CONTENT);
        }

        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.OPENING);
        xmlResponse.writeText(status);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.CLOSING);
    }

    private void handleFindPropNamesResponse(XmlWriter xmlResponse, boolean isFolder, String status) {
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.OPENING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.OPENING);
        xmlResponse.writeElement(PROP_NS_ABBRV1, "creationdate", XmlWriter.NO_CONTENT);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "displayname", XmlWriter.NO_CONTENT);
        if (!isFolder) {
            xmlResponse.writeElement(PROP_NS_ABBRV1, "getcontentlanguage", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "getcontentlength", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "getcontenttype", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "getetag", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "getlastmodified", XmlWriter.NO_CONTENT);
        }
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "resourcetype", XmlWriter.NO_CONTENT);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "source", XmlWriter.NO_CONTENT);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "lockdiscovery", XmlWriter.NO_CONTENT);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.OPENING);
        xmlResponse.writeText(status);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.CLOSING);
    }

    private void handleFindAllPropsResponse(XmlWriter xmlResponse, String creationDate, boolean isFolder,
            String lastModified,
            String resourceLength, String status, String path, String resourceName) throws IOException {
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.OPENING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.OPENING);
        if (!isFolder) {
            xmlResponse.writeProperty(PROP_NS_ABBRV1, "getcontentlength", resourceLength);

            MimeType ct = NamingUtils.getMimeType(path);
            xmlResponse.writeProperty(PROP_NS_ABBRV1, "getcontenttype", ct.getType());
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.NO_CONTENT);
        } else {
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.OPENING);
            xmlResponse.writeElement(DEFAULT_NS_ABBRV, "collection", XmlWriter.NO_CONTENT);
            xmlResponse.writeElement(PROP_NS_ABBRV1, "resourcetype", XmlWriter.CLOSING);
        }
        xmlResponse.writeProperty(PROP_NS_ABBRV1, "getlastmodified", lastModified);
        xmlResponse.writeProperty(PROP_NS_ABBRV1, "getetag", getEtag(resourceLength, lastModified));
        xmlResponse.writeProperty(PROP_NS_ABBRV1, "creationdate", creationDate);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "displayname", XmlWriter.OPENING);
        xmlResponse.writeData(resourceName);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "displayname", XmlWriter.CLOSING);
        xmlResponse.writeProperty(DEFAULT_NS_ABBRV, "source", "");
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.OPENING);
        xmlResponse.writeText(status);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "status", XmlWriter.CLOSING);
        xmlResponse.writeElement(DEFAULT_NS_ABBRV, "propstat", XmlWriter.CLOSING);
    }

    /**
     * goes recursive through all folders. used by propfind
     */
    private void recursiveParseProperties(ArtifactoryRequest request,
            XmlWriter generatedXml, BaseBrowsableItem currentItem, PropfindType propertyFindType,
            List<String> properties,
            int depth)
            throws IOException {

        parseProperties(request, generatedXml, currentItem, propertyFindType, properties);

        if (depth <= 0) {
            return;
        }

        if (currentItem.isFolder()) {
            BrowsableItemCriteria criteria = new BrowsableItemCriteria.Builder(currentItem.getRepoPath()).build();
            List<BaseBrowsableItem> browsableChildren = repoBrowsing.getLocalRepoBrowsableChildren(criteria);
            for (BaseBrowsableItem child : browsableChildren) {
                recursiveParseProperties(request, generatedXml, child,
                        propertyFindType, properties, depth - 1);
            }
        }
    }

    private List<String> getPropertiesFromXml(Node propNode) {
        List<String> properties;
        properties = new ArrayList<>();
        NodeList childList = propNode.getChildNodes();
        for (int i = 0; i < childList.getLength(); i++) {
            Node currentNode = childList.item(i);
            switch (currentNode.getNodeType()) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    String propertyName;
                    if (nodeName.indexOf(':') != -1) {
                        propertyName = nodeName.substring(nodeName.indexOf(':') + 1);
                    } else {
                        propertyName = nodeName;
                    }
                    // href is a live property which is handled differently
                    properties.add(propertyName);
                    break;
            }
        }
        return properties;
    }

}