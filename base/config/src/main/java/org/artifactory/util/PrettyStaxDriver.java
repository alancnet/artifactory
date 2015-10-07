package org.artifactory.util;

import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.QNameMap;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import javanet.staxutils.StaxUtilsXMLOutputFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * Stax driver configured to pretty print
 */
public class PrettyStaxDriver extends StaxDriver {
    private XMLOutputFactory outputFactory;

    public PrettyStaxDriver(QNameMap qNameMap) {
        super((qNameMap == null) ? new QNameMap() : qNameMap);
    }

    public PrettyStaxDriver(QNameMap qNameMap, NameCoder nameCoder) {
        super((qNameMap == null) ? new QNameMap() : qNameMap, nameCoder);
    }

    @Override
    public XMLOutputFactory getOutputFactory() {
        if (outputFactory == null) {
            //Decorate the original output factory - make it pretty print
            outputFactory = new StaxUtilsXMLOutputFactory(super.getOutputFactory()) {
                @Override
                public Object getProperty(String name) throws IllegalArgumentException {
                    //noinspection SimplifiableIfStatement
                    if (XMLOutputFactory.IS_REPAIRING_NAMESPACES.equals(name)) {
                        //Avoid delegating to the parent XOF, since may result in IAE (RTFACT-2193).
                        return false;
                    }
                    return super.getProperty(name);
                }
            };
            outputFactory.setProperty(StaxUtilsXMLOutputFactory.INDENTING, true);
        }
        return outputFactory;
    }

    @Override
    protected XMLInputFactory createInputFactory() {
        XMLInputFactory inputFactory = super.createInputFactory();
        inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return inputFactory;
    }
}
