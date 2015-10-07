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

import org.artifactory.api.request.ArtifactoryResponse;
import org.artifactory.repo.service.InternalRepositoryService;
import org.artifactory.repo.webdav.WebdavMethod;
import org.artifactory.repo.webdav.WebdavStatus;
import org.artifactory.repo.webdav.XmlWriter;
import org.artifactory.request.ArtifactoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.Writer;

import static org.artifactory.repo.webdav.WebdavServiceImpl.DEFAULT_NAMESPACE;
import static org.artifactory.repo.webdav.WebdavServiceImpl.DEFAULT_NS_ABBRV;

/**
 * @author Yoav Luft
 */
@Component
public class LockMethod extends AbstractWebdavMethod implements WebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(LockMethod.class);
    public static final String LOCK_TOKEN = "Artifactory:fake-lock-token";

    @Autowired
    private InternalRepositoryService repositoryService;

    @Override
    public boolean canHandle(String method) {
        return getName().equalsIgnoreCase(method);
    }

    @Override
    public void handle(ArtifactoryRequest request, ArtifactoryResponse response) throws IOException {
        log.debug("Handling {}", getName());
        if (request.getContentLength() <= 0) {
            response.sendError(WebdavStatus.SC_BAD_REQUEST, "Bad lock request", log);
        }
        DocumentBuilder documentBuilder = getDocumentBuilder();
        try {
            Document document = documentBuilder.parse(new InputSource(request.getInputStream()));
            Element root = document.getDocumentElement();
            String namespace = root.getNamespaceURI();
            String scope = getLockscope(root, namespace);
            String lockType = getLockType(root, namespace);
            String owner = getLockOwner(root, namespace);
            if ("read".equals(lockType) && !repositoryService.exists(request.getRepoPath())) {
                response.sendError(WebdavStatus.SC_NOT_FOUND, request.getRepoPath().toPath() + " not found", log);
            }
            Writer writer = response.getWriter();
            writeResponse(writer, owner, scope, lockType);
        } catch (SAXException e) {
            response.sendError(WebdavStatus.SC_BAD_REQUEST, "Bad lock request", log);
        }
        response.setHeader("Lock-Token", "<" + LOCK_TOKEN + ">");
        response.setHeader("Vary", "Accept-Encoding");
        response.setContentType("text/xml; charset=\"utf-8\"");
        response.sendSuccess();
    }

    private String getLockType(Element root, String namespace) {
        Node locktype = root.getElementsByTagNameNS(namespace, "locktype").item(0);
        if (locktype != null) {
            if (locktype.getFirstChild() != null) {
                return locktype.getFirstChild().getLocalName();
            }
        }
        return "";
    }

    private String getLockscope(Element root, String namespace) {
        Node lockscope = root.getElementsByTagNameNS(namespace, "lockscope").item(0);
        if (lockscope != null) {
            if (lockscope.getFirstChild() != null) {
                return lockscope.getFirstChild().getLocalName();
            }
        }
        return "";
    }

    private String getLockOwner(Element root, String namespace) {
        Node ownerElement = root.getElementsByTagNameNS(namespace, "owner").item(0);
        NodeList childNodes = ownerElement.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if ("href".equals(node.getLocalName())) {
                return node.getFirstChild().getNodeValue();
            }
        }
        return "";
    }

    private void writeResponse(Writer writer, String owner, String scope, String locktype) throws IOException {
        XmlWriter xmlWriter = new XmlWriter(writer);
        xmlWriter.writeXMLHeader();
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.OPENING, DEFAULT_NS_ABBRV, DEFAULT_NAMESPACE);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "lockdiscovery", XmlWriter.OPENING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "activelock", XmlWriter.OPENING);
        if (!locktype.isEmpty()) {
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "locktype", XmlWriter.OPENING);
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, locktype, XmlWriter.NO_CONTENT);
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "locktype", XmlWriter.CLOSING);
        } else {
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "locktype", XmlWriter.NO_CONTENT);
        }
        if (!scope.isEmpty()) {
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "lockscope", XmlWriter.OPENING);
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, scope, XmlWriter.NO_CONTENT);
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "lockscope", XmlWriter.CLOSING);
        } else {
            xmlWriter.writeElement(DEFAULT_NS_ABBRV, "lockscope", XmlWriter.NO_CONTENT);
        }
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "depth", XmlWriter.OPENING);
        xmlWriter.writeText("infinity");
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "depth", XmlWriter.CLOSING);
        if (!owner.isEmpty()) {
            xmlWriter.writeElement("ns0", "owner", XmlWriter.OPENING, "ns0", DEFAULT_NAMESPACE);
            xmlWriter.writeElement("ns0", "href", XmlWriter.OPENING);
            xmlWriter.writeText(owner);
            xmlWriter.writeElement("ns0", "href", XmlWriter.CLOSING);
            xmlWriter.writeElement("ns0", "owner", XmlWriter.CLOSING);
        } else {
            xmlWriter.writeElement("ns0", "owner", XmlWriter.NO_CONTENT, "ns0", DEFAULT_NAMESPACE);
        }
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "timeout", XmlWriter.OPENING);
        xmlWriter.writeText("Second-3600");
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "timeout", XmlWriter.CLOSING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "locktoken", XmlWriter.OPENING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "href", XmlWriter.OPENING);
        xmlWriter.writeText(LOCK_TOKEN);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "href", XmlWriter.CLOSING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "locktoken", XmlWriter.CLOSING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "activelock", XmlWriter.CLOSING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "lockdiscovery", XmlWriter.CLOSING);
        xmlWriter.writeElement(DEFAULT_NS_ABBRV, "prop", XmlWriter.CLOSING);
        xmlWriter.sendData();
    }

    @Override
    public String getName() {
        return "lock";
    }
}
