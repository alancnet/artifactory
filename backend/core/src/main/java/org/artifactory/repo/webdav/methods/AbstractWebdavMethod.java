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

import org.artifactory.repo.webdav.WebdavMethod;
import org.artifactory.request.ArtifactoryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import static org.artifactory.repo.webdav.WebdavServiceImpl.INFINITY;

/**
 * @author Yoav Luft
 */
public abstract class AbstractWebdavMethod implements WebdavMethod {

    private static final Logger log = LoggerFactory.getLogger(AbstractWebdavMethod.class);

    /**
     * Get the ETag associated with a file.
     */
    static String getEtag(String resourceLength, String lastModified)
            throws IOException {
        return "W/\"" + resourceLength + "-" + lastModified + "\"";
    }

    protected int findDepth(ArtifactoryRequest request) {
        String depthStr = request.getHeader("Depth");
        if (depthStr == null) {
            return INFINITY;
        }
        switch (depthStr) {
            case "0":
                return 0;
            case "1":
                return 1;
            case "infinity":
                return INFINITY;
            default:
                return INFINITY;
        }
    }

    /**
     * resource type Return JAXP document builder instance.
     *
     * @return
     * @throws javax.servlet.ServletException
     */
    protected DocumentBuilder getDocumentBuilder() throws IOException {
        DocumentBuilder documentBuilder;
        DocumentBuilderFactory documentBuilderFactory;
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("JAXP document builder creation failed", e);
        }
        return documentBuilder;
    }

    protected void logWebdavRequest(Document document) throws TransformerException {
        if (log.isDebugEnabled()) {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            log.debug("Webdav request body:\n" + writer.toString());
        }
    }
}
