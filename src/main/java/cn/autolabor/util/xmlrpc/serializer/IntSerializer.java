package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.XMLUtil;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 * @author timroes
 */
public class IntSerializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return Integer.parseInt(XMLUtil.getOnlyTextContent(content.getChildNodes()));
    }

    public XmlElement serialize(Object object) {
        return XMLUtil.makeXmlTag(SerializerHandler.TYPE_INT,
                object.toString());
    }

}
