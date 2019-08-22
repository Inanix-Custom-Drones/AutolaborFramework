package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 * @author Tim Roes
 */
public class NullSerializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return null;
    }

    public XmlElement serialize(Object object) {
        return new XmlElement(SerializerHandler.TYPE_NULL);
    }

}