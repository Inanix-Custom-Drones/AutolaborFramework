package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.XMLUtil;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 * @author Tim Roes
 */
public class BooleanSerializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return XMLUtil.getOnlyTextContent(content.getChildNodes()).equals("1")
                ? Boolean.TRUE : Boolean.FALSE;
    }

    public XmlElement serialize(Object object) {
        return XMLUtil.makeXmlTag(SerializerHandler.TYPE_BOOLEAN,
                (Boolean) object ? "1" : "0");
    }

}