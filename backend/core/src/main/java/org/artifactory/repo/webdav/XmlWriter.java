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

package org.artifactory.repo.webdav;

import java.io.IOException;
import java.io.Writer;

/**
 * XMLWriter helper class.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 */
public class XmlWriter {


    // -------------------------------------------------------------- Constants


    /**
     * Opening tag.
     */
    public static final int OPENING = 0;


    /**
     * Closing tag.
     */
    public static final int CLOSING = 1;


    /**
     * Element with no content.
     */
    public static final int NO_CONTENT = 2;


    // ----------------------------------------------------- Instance Variables


    /**
     * Buffer.
     */
    protected StringBuffer buffer = new StringBuffer();


    /**
     * Writer.
     */
    protected Writer writer = null;


    // ----------------------------------------------------------- Constructors


    /**
     * Constructor.
     */
    public XmlWriter() {
    }


    /**
     * Constructor.
     */
    public XmlWriter(Writer writer) {
        this.writer = writer;
    }


    // --------------------------------------------------------- Public Methods


    /**
     * Retrieve generated XML.
     *
     * @return String containing the generated XML
     */
    public String toString() {
        return buffer.toString();
    }


    /**
     * Write property to the XML.
     *
     * @param namespace     Namespace
     * @param namespaceInfo Namespace info
     * @param name          Property name
     * @param value         Property value
     */
    public void writeProperty(String namespace, String namespaceInfo,
            String name, String value) {
        writeElement(namespace, namespaceInfo, name, OPENING);
        buffer.append(value);
        writeElement(namespace, namespaceInfo, name, CLOSING);

    }


    /**
     * Write property to the XML.
     *
     * @param namespace Namespace
     * @param name      Property name
     * @param value     Property value
     */
    public void writeProperty(String namespace, String name, String value) {
        writeElement(namespace, name, OPENING);
        buffer.append(value);
        writeElement(namespace, name, CLOSING);
    }


    /**
     * Write property to the XML.
     *
     * @param namespace Namespace
     * @param name      Property name
     */
    public void writeProperty(String namespace, String name) {
        writeElement(namespace, name, NO_CONTENT);
    }


    /**
     * Write an element.
     *
     * @param name      Element name
     * @param namespace Namespace abbreviation
     * @param type      Element type
     */
    public void writeElement(String namespace, String name, int type) {
        writeElement(namespace, null, name, type);
    }

    /**
     *
     * @param name              Element name
     * @param type              Opening, closing or no content element
     * @param namespace         Namespace abbreviation
     * @param namespaceInfos    Namespace definitions
     */
    public void writeElement(String namespace, String name, int type, String... namespaceInfos) {
        if ((namespace != null) && (namespace.length() > 0)) {
            switch (type) {
                case OPENING:
                    if (namespaceInfos.length % 2 == 0) {
                        buffer.append("<").append(namespace).append(":").append(name);
                        for (int i = 0;  i < namespaceInfos.length; i += 2) {
                            buffer.append(" xmlns:").append(namespaceInfos[i]).append("=\"")
                                    .append(namespaceInfos[i + 1]).append("\"");
                        }
                        buffer.append(">");
                    }
                     else {
                        buffer.append("<" + namespace + ":" + name + ">");
                    }
                    break;
                case CLOSING:
                    buffer.append("</" + namespace + ":" + name + ">\n");
                    break;
                case NO_CONTENT:
                default:
                    if (namespaceInfos.length % 2 == 0) {
                        buffer.append("<").append(namespace).append(":").append(name);
                        for (int i = 0;  i < namespaceInfos.length; i += 2) {
                            buffer.append(" xmlns:").append(namespaceInfos[i]).append("=\"")
                                    .append(namespaceInfos[i + 1]).append("\"");
                        }
                        buffer.append("/>");
                    }
                    else {
                        buffer.append("<" + namespace + ":" + name + "/>");
                    }
                    break;
            }
        } else {
            switch (type) {
                case OPENING:
                    buffer.append("<" + name + ">");
                    break;
                case CLOSING:
                    buffer.append("</" + name + ">\n");
                    break;
                case NO_CONTENT:
                default:
                    buffer.append("<" + name + "/>");
                    break;
            }
        }
    }

    /**
     * Write an element.
     *
     * @param namespace     Namespace abbreviation
     * @param namespaceInfo Namespace info
     * @param name          Element name
     * @param type          Element type
     */
    public void writeElement(String namespace, String namespaceInfo,
            String name, int type) {
        if ((namespace != null) && (namespace.length() > 0)) {
            switch (type) {
                case OPENING:
                    if (namespaceInfo != null) {
                        buffer.append("<" + namespace + ":" + name + " xmlns:"
                                + namespace + "=\""
                                + namespaceInfo + "\">");
                    } else {
                        buffer.append("<" + namespace + ":" + name + ">");
                    }
                    break;
                case CLOSING:
                    buffer.append("</" + namespace + ":" + name + ">\n");
                    break;
                case NO_CONTENT:
                default:
                    if (namespaceInfo != null) {
                        buffer.append("<" + namespace + ":" + name + " xmlns:"
                                + namespace + "=\""
                                + namespaceInfo + "\"/>");
                    } else {
                        buffer.append("<" + namespace + ":" + name + "/>");
                    }
                    break;
            }
        } else {
            switch (type) {
                case OPENING:
                    buffer.append("<" + name + ">");
                    break;
                case CLOSING:
                    buffer.append("</" + name + ">\n");
                    break;
                case NO_CONTENT:
                default:
                    buffer.append("<" + name + "/>");
                    break;
            }
        }
    }


    /**
     * Write text.
     *
     * @param text Text to append
     */
    public void writeText(String text) {
        buffer.append(text);
    }


    /**
     * Write data.
     *
     * @param data Data to append
     */
    public void writeData(String data) {
        buffer.append("<![CDATA[" + data + "]]>");
    }


    /**
     * Write XML Header.
     */
    public void writeXMLHeader() {
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    }


    /**
     * Send data and reinitializes buffer.
     */
    public void sendData()
            throws IOException {
        if (writer != null) {
            writer.write(buffer.toString());
            buffer = new StringBuffer();
        }
    }


}
