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

package org.artifactory.config.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Yoav Landman
 */
public class EntityResolvingContentHandler extends DefaultHandler {
    private static final Logger log = LoggerFactory.getLogger(EntityResolvingContentHandler.class);
    private final ContentHandler handler;
    private final boolean namespaceAware;


    public EntityResolvingContentHandler(ContentHandler handler) {
        this(handler, false);
    }

    public EntityResolvingContentHandler(ContentHandler handler, boolean namespaceAware) {
        this.handler = handler;
        this.namespaceAware = namespaceAware;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        InputSource source = super.resolveEntity(publicId, systemId);
        if (source == null) {
            InputStream stream = this.getClass().getResourceAsStream("xml/characters.ent");
            if (stream != null) {
                source = new InputSource(stream);
            }
        }
        return source;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        handler.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        handler.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        handler.endDocument();
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        handler.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        handler.endPrefixMapping(prefix);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        handler.startElement(processUri(uri), localName, qName, atts);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        handler.endElement(processUri(uri), localName, qName);
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        handler.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        handler.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        handler.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        handler.skippedEntity(name);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        //Temp hack to avoid broken plexus poms (ver 1.0.4 & 1.0.5)
        log.warn("Received the following error during xml parsing: '" + e.getMessage() + "'.");
        //No 'super.fatalError(e)'!;
    }

    private String processUri(String uri) {
        return namespaceAware ? uri : "";
    }
}