package org.artifactory.logging.version.v3;

import org.apache.commons.lang.StringUtils;
import org.artifactory.convert.XmlConverterTest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class LogbackJFrogInfoConverterTest extends XmlConverterTest {

    @Test
    public void loggerIsMissing() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v4/logback.xml", new LogbackJFrogInfoConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        int count = 0;
        List<Element> loggers = root.getChildren("logger", ns);
        for (Element logger : loggers) {
            if (StringUtils.equals(logger.getAttributeValue("name", ns), "org.jfrog")) {
                count++;
                assertNull(logger.getAttributeValue("value"));
                assertEquals(logger.getChild("level", ns).getAttributeValue("value", ns), "info");
            }
        }

        assertEquals(count, 1);
    }

    @Test
    public void loggerExists() throws Exception {
        Document doc = convertXml("/org/artifactory/logging/version/v5/logback.xml", new LogbackJFrogInfoConverter());
        Element root = doc.getRootElement();
        Namespace ns = root.getNamespace();

        int count = 0;
        List<Element> loggers = root.getChildren("logger", ns);
        for (Element logger : loggers) {
            if (StringUtils.equals(logger.getAttributeValue("name", ns), "org.jfrog")) {
                count++;
            }
        }

        assertEquals(count, 1);
    }
}